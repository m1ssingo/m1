package dev.m1ssing0.packclient.mixin;

import dev.m1ssing0.packclient.SteelButtonStyle;
import dev.m1ssing0.packclient.SteelButtonStyle.State;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.widgets.CenteredFlatWidget", remap = false)
abstract class SodiumCategorySkinMixin {
    @Shadow @Final private boolean isSelectable;
    @Shadow private boolean enabled;
    @Shadow private boolean selected;

    @Redirect(method = "render", at = @At(value = "INVOKE", ordinal = 0,
        target = "Lnet/caffeinemc/mods/sodium/client/gui/widgets/CenteredFlatWidget;drawRect"
            + "(Lnet/minecraft/client/gui/GuiGraphics;IIIII)V"))
    private void m1$drawCategory(@Coerce Object button, GuiGraphics graphics, int x, int y, int right, int bottom,
                                  int color, GuiGraphics renderGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isSelectable) {
            graphics.fill(x, y, right, bottom, color);
            return;
        }
        GuiEventListener widget = (GuiEventListener) button;
        boolean highlighted = selected || widget.isFocused() || widget.isMouseOver(mouseX, mouseY);
        SteelButtonStyle.draw(graphics, x, y, right - x, bottom - y, State.of(enabled, highlighted));
    }

    @ModifyArg(method = "render", index = 5, at = @At(value = "INVOKE", ordinal = 1,
        target = "Lnet/caffeinemc/mods/sodium/client/gui/widgets/CenteredFlatWidget;drawRect"
            + "(Lnet/minecraft/client/gui/GuiGraphics;IIIII)V"))
    private int m1$useOrangeIndicator(int originalColor) {
        return isSelectable ? SteelButtonStyle.ORANGE : originalColor;
    }
}
