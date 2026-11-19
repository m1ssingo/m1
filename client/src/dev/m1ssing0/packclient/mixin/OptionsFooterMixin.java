package dev.m1ssing0.packclient.mixin;

import dev.m1ssing0.packclient.PackClient;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
abstract class OptionsFooterMixin {
    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void m1packclient$restoreOptionsFooter(CallbackInfo callback) {
        // Vanilla repositions Done when returning to Options or resizing its window.
        PackClient.arrangeOptionsFooter((OptionsScreen) (Object) this);
    }
}
