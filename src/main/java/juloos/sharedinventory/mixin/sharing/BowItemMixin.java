package juloos.sharedinventory.mixin.sharing;

import juloos.sharedinventory.Inventories;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public abstract class BowItemMixin {
    @Shadow public abstract void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i);

    @Inject(
            method = "releaseUsing",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sharedinventory$releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i, CallbackInfo ci) {
        if (livingEntity instanceof Player player)
            Inventories.tryOperateAsPlayer(player, inv -> {
                this.releaseUsing(itemStack, level, player, i);
                ci.cancel();
            });
    }
}
