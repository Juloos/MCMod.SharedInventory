package juloos.sharedinventory.mixin.sharing;

import juloos.sharedinventory.Inventories;
import juloos.sharedinventory.bridge.InventoryBridge;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class InventoryMixin implements InventoryBridge {
    @Shadow public abstract int getContainerSize();
    @Shadow public abstract void setItem(int i, ItemStack itemStack);
    @Shadow public abstract boolean add(int i, ItemStack itemStack);

    @Shadow @Final public Player player;


    @Override
    public void sharedinventory$replaceWithoutSelect(Inventory inventory) {
        for (int i = 0; i < this.getContainerSize(); i++)
            this.setItem(i, inventory.getItem(i));
    }


    @Inject(
            method = "pickSlot",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sharedinventory$pickSlot(int i, CallbackInfo ci) {
        Inventories.tryOperateOnInventory(this.player.getTeam(), inv -> {
            inv.pickSlot(i);
            ci.cancel();
        });
    }

    @Inject(
            method = "add(ILnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sharedinventory$add(int i, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        Inventories.tryOperateAsPlayer(this.player, inv -> {
            cir.setReturnValue(this.add(i, itemStack));
        });
    }
}
