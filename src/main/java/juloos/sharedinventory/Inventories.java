package juloos.sharedinventory;

import com.mojang.authlib.GameProfile;
import juloos.sharedinventory.bridge.InventoryBridge;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class Inventories {
    // We should ensure individual synchronization for each entry of the map
    private static final Map<PlayerTeam, ConcurrentInventory> inventories = new HashMap<>();

    private static final Path savePath;
    static {
        savePath = FabricLoader.getInstance().getConfigDir().resolve(juloos.sharedinventory.SharedInventory.MODID).resolve("inventories");
        try {
            Files.createDirectories(savePath);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void add(ServerLevel serverLevel, PlayerTeam team) {
        inventories.put(team, new ConcurrentInventory(FakePlayer.get(serverLevel, new GameProfile(FakePlayer.DEFAULT_UUID, team.getName()))));
    }

    public static void remove(PlayerTeam playerTeam) {
        inventories.remove(playerTeam);
        try {
            Files.deleteIfExists(savePath.resolve(playerTeam.getName()));
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static boolean tryOperateOnInventory(PlayerTeam team, Consumer<Inventory> consumer) {
        if (!SharedInventory.enable)
            return false;
        ConcurrentInventory inv = inventories.get(team);
        if (inv == null || inv.getLockHoldCount() >= 1)
            return false;
        assert inv.player.getServer() != null;
        inv.lock();
        try {
            consumer.accept(inv);
            if (inv.getLockHoldCount() == 1)
                broadcastReplaceWith(inv.player.getServer().getPlayerList(), team);
        } finally {
            inv.unlock();
        }
        return true;
    }

    public static boolean tryOperateAsPlayer(Player player, Consumer<Inventory> consumer) {
        if (player == null || !SharedInventory.enable)
            return false;
        ConcurrentInventory inv = inventories.get(player.getTeam());
        if (inv == null || inv.getLockHoldCount() >= 1)
            return false;
        assert player.getServer() != null;
        inv.lock();
        try {
            consumer.accept(player.getInventory());
            inv.replaceWith(player.getInventory());
            broadcastReplaceWith(player.getServer().getPlayerList(), player.getTeam());
        } finally {
            inv.unlock();
        }
        return true;
    }

    private static void broadcastReplaceWith(PlayerList playerList, PlayerTeam playerTeam) {
        Inventory teamInventory = inventories.get(playerTeam);
        playerTeam.getPlayers().stream().map(playerList::getPlayerByName).filter(Objects::nonNull).forEach(player -> {
            ((InventoryBridge) player.getInventory()).sharedinventory$replaceWithoutSelect(teamInventory);
            player.inventoryMenu.broadcastFullState();
        });
    }

    public static void save() {
        inventories.forEach((team, inventory) -> {
            if (inventory == null)
                return;
            try {
                Files.createDirectories(savePath);
                CompoundTag compoundTag = new CompoundTag();
                tryOperateOnInventory(team, inv -> compoundTag.put("Inventory", inv.save(new ListTag())));
                NbtIo.writeCompressed(compoundTag, savePath.resolve(team.getName()));
            } catch (Exception e) {
                System.err.println(e);
            }
        });
    }

    public static void load(ServerLevel serverLevel) {
        Map<String, PlayerTeam> name2team = new HashMap<>();
        for (PlayerTeam team : serverLevel.getServer().getScoreboard().getPlayerTeams()) {
            add(serverLevel, team);
            name2team.put(team.getName(), team);
        }
        try {
            try (var teams = Files.find(savePath, 1, (path, attr) -> true)) {
                teams.filter(p -> p.toFile().isFile()).map(Path::getFileName).map(Path::toString).forEach(team -> {
                    try {
                        ListTag listTag = (ListTag) NbtIo.readCompressed(savePath.resolve(team), NbtAccounter.unlimitedHeap()).get("Inventory");
                        tryOperateOnInventory(name2team.get(team), inv -> inv.load(listTag));
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                });
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private static class ConcurrentInventory extends Inventory {
        private final ReentrantLock lock = new java.util.concurrent.locks.ReentrantLock();

        ConcurrentInventory(Player player) {
            super(player);
        }

        int getLockHoldCount() {
            return lock.getHoldCount();
        }

        void lock() {
            lock.lock();
        }

        void unlock() {
            lock.unlock();
        }
    }
}
