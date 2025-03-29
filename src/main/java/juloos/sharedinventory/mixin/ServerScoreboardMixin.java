package juloos.sharedinventory.mixin;

import juloos.sharedinventory.Inventories;
import juloos.sharedinventory.SharedInventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerScoreboard.class)
public abstract class ServerScoreboardMixin extends Scoreboard {

    @Shadow @Final public MinecraftServer server;

    @Inject(
            method = "onTeamAdded",
            at = @At("HEAD")
    )
    private void sharedinventory$onTeamAdded(PlayerTeam playerTeam, CallbackInfo ci) {
        Inventories.add(((ServerScoreboard) (Object) this).server.overworld(), playerTeam);
    }

    @Inject(
            method = "onTeamRemoved",
            at = @At("HEAD")
    )
    private void sharedinventory$onTeamRemoved(PlayerTeam playerTeam, CallbackInfo ci) {
        Inventories.remove(playerTeam);
    }

    @Inject(
            method = "addPlayerToTeam",
            at = @At("HEAD")
    )
    private void sharedinventory$addPlayerToTeamHEAD(String string, PlayerTeam playerTeam, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer serverPlayer = server.getPlayerList().getPlayerByName(string);
        if (serverPlayer == null || !SharedInventory.enable)
            return;
        PlayerTeam oldPlayerTeam = serverPlayer.getTeam();
        if (
            !playerTeam.getPlayers().isEmpty() && (
                 oldPlayerTeam == null ||
                (oldPlayerTeam != playerTeam && oldPlayerTeam.getPlayers().size() == 1)
            )
        ) {  // Drop everything on the ground
            for (List<ItemStack> list : serverPlayer.getInventory().compartments)
                for (ItemStack itemStack : list)
                    if (!itemStack.isEmpty())
                        serverPlayer.drop(itemStack, false, false);
            serverPlayer.getInventory().clearContent();
        }
    }

    @Inject(
            method = "addPlayerToTeam",
            at = @At(value = "RETURN", ordinal = 0)
    )
    private void sharedinventory$addPlayerToTeamRETURN(String string, PlayerTeam playerTeam, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer serverPlayer = server.getPlayerList().getPlayerByName(string);
        if (serverPlayer == null || !SharedInventory.enable)
            return;
        if (playerTeam.getPlayers().size() > 1) {
            int selected = serverPlayer.getInventory().selected;
            Inventories.tryOperateOnInventory(playerTeam, inv -> serverPlayer.getInventory().replaceWith(inv));
            serverPlayer.getInventory().selected = selected;
        }
    }

    @Inject(
            method = "removePlayerFromTeam",
            at = @At("RETURN")
    )
    private void sharedinventory$removePlayerFromTeam(String string, PlayerTeam playerTeam, CallbackInfo ci) {
        ServerPlayer serverPlayer = server.getPlayerList().getPlayerByName(string);
        if (serverPlayer == null || !SharedInventory.enable)
            return;
        if (!playerTeam.getPlayers().isEmpty())
            serverPlayer.getInventory().clearContent();
        else
            Inventories.tryOperateOnInventory(playerTeam, Inventory::clearContent);
        // Check case when last player is offline
    }
}
