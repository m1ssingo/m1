package dev.m1ssing0.packclient.mixin;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionSpecialEffects.class)
abstract class CloudHeightMixin {
    @Inject(method = "getCloudHeight", at = @At("RETURN"), cancellable = true)
    private void m1$liftClouds(CallbackInfoReturnable<Float> result) {
        float height = result.getReturnValueF();
        if (Float.isFinite(height)) result.setReturnValue(height + 50.0f);
    }
}
