package juloos.sharedinventory.mixin.sharing;

import juloos.sharedinventory.Inventories;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Shadow protected abstract void doClick(int i, int j, ClickType clickType, Player player);


    @Inject(
            method = "doClick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sharedinventory$doClick(int i, int j, ClickType clickType, Player player, CallbackInfo ci) {
        Inventories.tryOperateAsPlayer(player, inv -> {
            this.doClick(i, j, clickType, player);
            ci.cancel();
        });
    }
}
