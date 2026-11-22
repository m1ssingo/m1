package dev.m1ssing0.packclient.mixin;

import dev.m1ssing0.packclient.SteelButtonStyle;
import dev.m1ssing0.packclient.SteelButtonStyle.Icon;
import dev.m1ssing0.packclient.SteelButtonStyle.State;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
abstract class WidgetSkinMixin {
    @Inject(method = "blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", at = @At("HEAD"), cancellable = true)
    private void m1$drawSteelWidget(ResourceLocation sprite, int x, int y, int width, int height, CallbackInfo callback) {
        if (!sprite.getNamespace().equals("minecraft")) return;

        GuiGraphics graphics = (GuiGraphics) (Object) this;
        switch (sprite.getPath()) {
            case "widget/button", "widget/slider", "widget/tab" ->
                SteelButtonStyle.draw(graphics, x, y, width, height, State.NORMAL);
            case "widget/button_highlighted", "widget/slider_highlighted", "widget/tab_highlighted",
                 "widget/tab_selected_highlighted", "widget/tab_selected" ->
                SteelButtonStyle.draw(graphics, x, y, width, height, State.HIGHLIGHTED);
            case "widget/button_disabled" ->
                SteelButtonStyle.draw(graphics, x, y, width, height, State.DISABLED);
            case "widget/checkbox" ->
                SteelButtonStyle.draw(graphics, x, y, width, height, State.NORMAL);
            case "widget/checkbox_highlighted" ->
                SteelButtonStyle.draw(graphics, x, y, width, height, State.HIGHLIGHTED);
            case "widget/checkbox_selected", "widget/checkbox_selected_highlighted" ->
                SteelButtonStyle.drawIcon(graphics, x, y, width, height, m1$iconState(sprite), Icon.CHECK);
            case "widget/locked_button", "widget/locked_button_highlighted", "widget/locked_button_disabled" ->
                SteelButtonStyle.drawIcon(graphics, x, y, width, height, m1$iconState(sprite), Icon.LOCKED);
            case "widget/unlocked_button", "widget/unlocked_button_highlighted", "widget/unlocked_button_disabled" ->
                SteelButtonStyle.drawIcon(graphics, x, y, width, height, m1$iconState(sprite), Icon.UNLOCKED);
            case "widget/cross_button", "widget/cross_button_highlighted" ->
                SteelButtonStyle.drawIcon(graphics, x, y, width, height, m1$iconState(sprite), Icon.CLOSE);
            case "widget/page_forward", "widget/page_forward_highlighted" ->
                SteelButtonStyle.drawIcon(graphics, x, y, width, height, m1$iconState(sprite), Icon.FORWARD);
            case "widget/page_backward", "widget/page_backward_highlighted" ->
                SteelButtonStyle.drawIcon(graphics, x, y, width, height, m1$iconState(sprite), Icon.BACK);
            case "widget/slider_handle" ->
                SteelButtonStyle.drawSliderHandle(graphics, x, y, width, height, false);
            case "widget/slider_handle_highlighted" ->
                SteelButtonStyle.drawSliderHandle(graphics, x, y, width, height, true);
            default -> {
                return;
            }
        }
        callback.cancel();
    }

    private static State m1$iconState(ResourceLocation sprite) {
        return State.of(!sprite.getPath().endsWith("_disabled"), sprite.getPath().endsWith("_highlighted"));
    }
}
