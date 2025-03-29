package juloos.sharedinventory.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import juloos.sharedinventory.Inventories;
import juloos.sharedinventory.SharedInventory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ShareCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(Commands.literal("share")
            .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
            .then(Commands.argument("enable", BoolArgumentType.bool())
                .executes(
                    context -> {
                        boolean enable = BoolArgumentType.getBool(context, "enable");
                        if (!enable) {
                            Inventories.save();
                        }
                        SharedInventory.enable = enable;
                        context.getSource().sendSystemMessage(
                            Component.literal("Team inventory sharing is now " + (enable ? "enabled" : "disabled"))
                        );
                        return 1;
                    }
        )));
    }
}
