package juloos.sharedinventory.mixin.sharing;

import juloos.sharedinventory.Inventories;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlaceRecipe.class)
public abstract class ServerPlaceRecipeMixin<I extends RecipeInput, R extends Recipe<I>> implements PlaceRecipe<Integer> {
    @Shadow public abstract void recipeClicked(ServerPlayer serverPlayer, @Nullable RecipeHolder<R> recipeHolder, boolean bl);

    @Inject(
            method = "recipeClicked",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sharedinventory$recipeClicked(ServerPlayer serverPlayer, @Nullable RecipeHolder<R> recipeHolder, boolean bl, CallbackInfo ci) {
        Inventories.tryOperateAsPlayer(serverPlayer, inv -> {
            this.recipeClicked(serverPlayer, recipeHolder, bl);
            ci.cancel();
        });
    }
}
