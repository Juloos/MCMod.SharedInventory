package juloos.sharedinventory;

import juloos.sharedinventory.command.ShareCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.entity.player.Inventory;

public class SharedInventory implements ModInitializer {
    public static final String MODID = "sharedinventory";

    public static boolean enable = true;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> ShareCommand.register(dispatcher));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> Inventories.load(server.overworld()));
        ServerLifecycleEvents.AFTER_SAVE.register((server, flush, force) -> Inventories.save());

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (handler.player.getTeam() == null || !enable)
                return;
            Inventory inventory = handler.player.getInventory();
            Inventories.tryOperateOnInventory(handler.player.getTeam(), inv -> {
                int selected = inventory.selected;
                inventory.replaceWith(inv);
                inventory.selected = selected;
            });
        });
    }
}
