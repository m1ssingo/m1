package dev.m1ssing0.packclient.mixin;

import dev.m1ssing0.packclient.PackTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
abstract class MenuBlurMixin {
    @Redirect(method = "processBlurEffect", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/Options;getMenuBackgroundBlurriness()I"))
    private int m1$menuBlurRadius(Options options) {
        // The vanilla menu scale runs from 0 to 10.
        var client = Minecraft.getInstance();
        return client.level == null || PackTitleScreen.isWorldTransition(client.screen)
            ? 3 : options.getMenuBackgroundBlurriness();
    }
}
