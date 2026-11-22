package dev.m1ssing0.packclient.mixin;

import dev.m1ssing0.packclient.SteelButtonStyle;
import dev.m1ssing0.packclient.SteelButtonStyle.State;
import mezz.jei.common.gui.elements.ScalableDrawable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "mezz.jei.gui.elements.InternalIconButton", remap = false)
abstract class JeiButtonSkinMixin {
    @Shadow private boolean pressed;
    @Shadow private boolean forcePressed;

    @Redirect(method = "renderWidget", at = @At(value = "INVOKE",
        target = "Lmezz/jei/common/gui/elements/ScalableDrawable;draw(Lnet/minecraft/client/gui/GuiGraphics;IIII)V"))
    private void m1$drawSteelBackground(ScalableDrawable background, GuiGraphics graphics, int x, int y,
                                        int width, int height) {
        AbstractWidget button = (AbstractWidget) (Object) this;
        SteelButtonStyle.draw(graphics, x, y, width, height,
            State.of(button.active, pressed || forcePressed || button.isHoveredOrFocused()));
    }
}
