package juloos.sharedinventory.mixin.sharing;

import com.llamalad7.mixinextras.sugar.Local;
import juloos.sharedinventory.Inventories;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    @Shadow public abstract void handleUseItem(ServerboundUseItemPacket serverboundUseItemPacket);
    @Shadow public abstract void handleUseItemOn(ServerboundUseItemOnPacket serverboundUseItemOnPacket);


    @Inject(
            method = "handlePlayerAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void sharedinventory$handlePlayerAction(ServerboundPlayerActionPacket serverboundPlayerActionPacket, CallbackInfo ci) {
        Inventories.tryOperateAsPlayer(this.player, inv -> {
            ItemStack itemStack = this.player.getItemInHand(InteractionHand.OFF_HAND);
            this.player.setItemInHand(InteractionHand.OFF_HAND, this.player.getItemInHand(InteractionHand.MAIN_HAND));
            this.player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
            this.player.stopUsingItem();
            ci.cancel();
        });
    }

    @Inject(
            method = "handleUseItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sharedinventory$handleUseItem(ServerboundUseItemPacket serverboundUseItemPacket, CallbackInfo ci) {
        Inventories.tryOperateAsPlayer(this.player, inv -> {
            this.handleUseItem(serverboundUseItemPacket);
            ci.cancel();
        });
    }

    @Inject(
            method = "handleUseItemOn",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sharedinventory$handleUseItem(ServerboundUseItemOnPacket serverboundUseItemOnPacket, CallbackInfo ci) {
        Inventories.tryOperateAsPlayer(this.player, inv -> {
            this.handleUseItemOn(serverboundUseItemOnPacket);
            ci.cancel();
        });
    }

    @Inject(
            method = "handleSetCreativeModeSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/InventoryMenu;getSlot(I)Lnet/minecraft/world/inventory/Slot;"
            ),
            cancellable = true
    )
    private void sharedinventory$handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket serverboundSetCreativeModeSlotPacket, CallbackInfo ci, @Local ItemStack itemStack) {
        Inventories.tryOperateAsPlayer(this.player, inv -> {
            this.player.inventoryMenu.getSlot(serverboundSetCreativeModeSlotPacket.slotNum()).setByPlayer(itemStack);
            ci.cancel();
        });
    }
}
