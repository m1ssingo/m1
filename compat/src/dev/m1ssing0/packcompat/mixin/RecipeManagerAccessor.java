package dev.m1ssing0.packcompat.mixin;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RecipeManager.class, remap = false)
public interface RecipeManagerAccessor {
    @Accessor("byName")
    Map<ResourceLocation, RecipeHolder<?>> m1compat$getUnfilteredByName();
}
