package juloos.sharedinventory.mixin.sharing;

import juloos.sharedinventory.Inventories;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow public abstract void playerTouch(Player player);

    @Inject(
            method = "playerTouch",
            at = @At("HEAD"),
            cancellable = true
    )
    private void playerTouch(Player player, CallbackInfo ci) {
        Inventories.tryOperateAsPlayer(player, inv -> {
            this.playerTouch(player);
            ci.cancel();
        });
    }
}
