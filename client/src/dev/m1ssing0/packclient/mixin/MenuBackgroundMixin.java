package dev.m1ssing0.packclient.mixin;

import dev.m1ssing0.packclient.PackTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
abstract class MenuBackgroundMixin {
    @Shadow protected Minecraft minecraft;
    @Shadow public int width;
    @Shadow public int height;

    @Inject(method = "renderPanorama", at = @At("HEAD"), cancellable = true)
    private void m1$usePackArtwork(GuiGraphics graphics, float partialTick, CallbackInfo callback) {
        if (minecraft.level != null) return;

        PackTitleScreen.drawSubmenuBackdrop(graphics, width, height);
        callback.cancel();
    }

    @Inject(method = "renderMenuBackground(Lnet/minecraft/client/gui/GuiGraphics;IIII)V",
        at = @At("HEAD"), cancellable = true)
    private void m1$shadePackArtwork(GuiGraphics graphics, int x, int y, int w, int h, CallbackInfo callback) {
        if (minecraft.level != null) return;

        graphics.fill(x, y, x + w, y + h, 0x66090e13);
        callback.cancel();
    }
}
