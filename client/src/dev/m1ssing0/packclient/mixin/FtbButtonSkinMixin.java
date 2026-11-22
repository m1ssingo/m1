package dev.m1ssing0.packclient.mixin;

import dev.ftb.mods.ftblibrary.ui.NordTheme;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.WidgetType;
import dev.m1ssing0.packclient.SteelButtonStyle;
import dev.m1ssing0.packclient.SteelButtonStyle.State;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {Theme.class, NordTheme.class}, remap = false)
abstract class FtbButtonSkinMixin {
    @Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
    private void m1$drawSteelButton(GuiGraphics graphics, int x, int y, int width, int height, WidgetType type,
                                   CallbackInfo callback) {
        SteelButtonStyle.draw(graphics, x, y, width, height, State.of(type != WidgetType.DISABLED,
            type == WidgetType.MOUSE_OVER));
        callback.cancel();
    }
}
