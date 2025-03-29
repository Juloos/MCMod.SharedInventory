package juloos.sharedinventory.mixin.sharing;

import juloos.sharedinventory.Inventories;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow protected abstract void dropAllDeathLoot(ServerLevel serverLevel, DamageSource damageSource);

    @Inject(
            method = "dropAllDeathLoot",
            at = @At("HEAD"),
            cancellable = true
    )
    private void dropAllDeathLoot(ServerLevel serverLevel, DamageSource damageSource, CallbackInfo ci) {
        if (((LivingEntity) (Object) this) instanceof Player playerEntity) {
            Inventories.tryOperateAsPlayer(playerEntity, inv -> {
                this.dropAllDeathLoot(serverLevel, damageSource);
                ci.cancel();
            });
        }
    }
}
