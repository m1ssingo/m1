package dev.m1ssing0.packclient.mixin;

import dev.m1ssing0.packclient.PackTitleScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ReceivingLevelScreen.class, GenericMessageScreen.class})
abstract class WorldTransitionBackgroundMixin extends Screen {
    protected WorldTransitionBackgroundMixin(Component title) {
        super(title);
    }

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void m1$drawTransition(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
                                    CallbackInfo callback) {
        if (!PackTitleScreen.isWorldTransition(this)) return;

        PackTitleScreen.drawSubmenuBackdrop(graphics, width, height);
        renderBlurredBackground(partialTick);
        graphics.fill(0, 0, width, height, 0x66090e13);
        callback.cancel();
    }
}
