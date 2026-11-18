package dev.m1ssing0.packcompat.mixin;

import java.util.Collection;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Golem Core prunes optional recipes by replacing the whole recipe collection.
 * History Stages filters getRecipes() according to current research. Feeding
 * that view into replaceRecipes() permanently deletes temporarily locked data.
 * Only this destructive pass must read the current unfiltered backing map.
 */
@Mixin(targets = "dev.golem.core.world.RecipeTables", remap = false)
public abstract class GolemRecipeTablesMixin {
    @Redirect(
        method = "removeListedRecipes",
        at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipes()Ljava/util/Collection;"),
        require = 1
    )
    private static Collection<RecipeHolder<?>> m1compat$preserveLockedRecipes(RecipeManager manager) {
        return ((RecipeManagerAccessor) (Object) manager).m1compat$getUnfilteredByName().values();
    }
}
