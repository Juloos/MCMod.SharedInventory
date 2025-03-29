package juloos.sharedinventory.mixin.sharing;

import juloos.sharedinventory.Inventories;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin {
    @Shadow public abstract InteractionResult interactAt(Player player, Vec3 vec3, InteractionHand interactionHand);

    @Inject(
            method = "interactAt",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sharedinventory$interactAt(Player player, Vec3 vec3, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        Inventories.tryOperateAsPlayer(player, inv -> {
            cir.setReturnValue(this.interactAt(player, vec3, interactionHand));
        });
    }
}
