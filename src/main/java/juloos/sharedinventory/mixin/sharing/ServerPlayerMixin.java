package juloos.sharedinventory.mixin.sharing;

import com.mojang.authlib.GameProfile;
import juloos.sharedinventory.Inventories;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    @Shadow public abstract boolean drop(boolean bl);
    @Shadow public abstract void restoreFrom(ServerPlayer serverPlayer, boolean bl);

    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }


    @Inject(
            method = "drop(Z)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sharedinventory$drop(boolean bl, CallbackInfoReturnable<Boolean> cir) {
        Inventories.tryOperateAsPlayer(this, inv -> {
            cir.setReturnValue(this.drop(bl));
        });
    }

    @Inject(
            method = "restoreFrom",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sharedinventory$restoreFrom(ServerPlayer serverPlayer, boolean bl, CallbackInfo ci) {
        Inventories.tryOperateAsPlayer(this, inv -> {
            this.restoreFrom(serverPlayer, bl);
            ci.cancel();
        });
    }
}
