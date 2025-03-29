package juloos.sharedinventory.mixin.sharing;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import juloos.sharedinventory.Inventories;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ItemCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(ItemCommands.class)
public abstract class ItemCommandsMixin {
    @Shadow private static ItemStack applyModifier(CommandSourceStack commandSourceStack, Holder<LootItemFunction> holder, ItemStack itemStack) {
        return null;
    }

    @Shadow @Final private static DynamicCommandExceptionType ERROR_TARGET_NO_CHANGES;

    @Shadow @Final private static Dynamic2CommandExceptionType ERROR_TARGET_NO_CHANGES_KNOWN_ITEM;

    /**
     * @author Juloos
     * @reason Shared inventory support
     */
    @Overwrite
    private static int modifyEntityItem(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, int i, Holder<LootItemFunction> holder) throws CommandSyntaxException {
        Map<Entity, ItemStack> map = Maps.newHashMapWithExpectedSize(collection.size());

        for (Entity entity : collection) {
            SlotAccess slotAccess = entity.getSlot(i);
            if (slotAccess != SlotAccess.NULL) {
                if (entity instanceof Player player &&
                        Inventories.tryOperateAsPlayer(player, inv -> {
                            ItemStack itemStack = applyModifier(commandSourceStack, holder, slotAccess.get().copy());
                            if (slotAccess.set(itemStack))
                                map.put(entity, itemStack);
                        }))
                    continue;
                ItemStack itemStack = applyModifier(commandSourceStack, holder, slotAccess.get().copy());
                if (slotAccess.set(itemStack))
                    map.put(entity, itemStack);
            }
        }

        if (map.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES.create(i);
        } else {
            if (map.size() == 1) {
                Map.Entry<Entity, ItemStack> entry = map.entrySet().iterator().next();
                commandSourceStack.sendSuccess(
                        () -> Component.translatable(
                                "commands.item.entity.set.success.single", (entry.getKey()).getDisplayName(), (entry.getValue()).getDisplayName()
                        ),
                        true
                );
            } else {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.item.entity.set.success.multiple", map.size()), true);
            }

            return map.size();
        }
    }

    /**
     * @author Juloos
     * @reason Shared inventory support
     */
    @Overwrite
    private static int setEntityItem(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, int i, ItemStack itemStack) throws CommandSyntaxException {
        List<Entity> list = Lists.<Entity>newArrayListWithCapacity(collection.size());

        for (Entity entity : collection) {
            SlotAccess slotAccess = entity.getSlot(i);
            if (slotAccess != SlotAccess.NULL) {
                if (entity instanceof Player player &&
                        Inventories.tryOperateAsPlayer(player, inv -> {
                            if (slotAccess.set(itemStack.copy()))
                                list.add(entity);
                        }))
                    continue;
                if (slotAccess.set(itemStack.copy()))
                    list.add(entity);
            }
        }

        if (list.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(itemStack.getDisplayName(), i);
        } else {
            if (list.size() == 1) {
                commandSourceStack.sendSuccess(
                        () -> Component.translatable("commands.item.entity.set.success.single", (list.getFirst()).getDisplayName(), itemStack.getDisplayName()),
                        true
                );
            } else {
                commandSourceStack.sendSuccess(() -> Component.translatable("commands.item.entity.set.success.multiple", list.size(), itemStack.getDisplayName()), true);
            }

            return list.size();
        }
    }
}
