package dev.m1ssing0.packclient;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Mod(value = "m1packclient", dist = Dist.CLIENT)
@EventBusSubscriber(modid = "m1packclient", value = Dist.CLIENT)
public final class PackClient {
    private static final Component OPENFM_AUDIO = Component.literal("OpenFM Audio...");
    private static boolean audited;

    @SubscribeEvent
    public static void openScreen(ScreenEvent.Opening event) {
        if (Boolean.getBoolean("m1pack.smoke") && event.getNewScreen() != null) {
            LogUtils.getLogger().info("[M1-SCREEN] {}", event.getNewScreen().getClass().getSimpleName());
        }
        if (event.getNewScreen() instanceof TitleScreen) {
            if (!audited) {
                KeyAudit.apply(Minecraft.getInstance());
                audited = true;
            }
            event.setNewScreen(new PackTitleScreen());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void arrangeOptionsFooter(ScreenEvent.Init.Post event) {
        // OpenFM adds its own button during Init.Post, after the vanilla footer is laid out.
        if (event.getScreen() instanceof OptionsScreen screen) arrangeOptionsFooter(screen);
    }

    public static void arrangeOptionsFooter(OptionsScreen screen) {
        Button openFm = null;
        Button done = null;
        for (var listener : screen.children()) {
            if (listener instanceof Button button) {
                if (OPENFM_AUDIO.equals(button.getMessage())) openFm = button;
                if (CommonComponents.GUI_DONE.equals(button.getMessage())) done = button;
            }
        }
        if (openFm == null || done == null) return;

        int gap = 8;
        int buttonWidth = Math.min(150, (screen.width - gap - 16) / 2);
        int left = (screen.width - 2 * buttonWidth - gap) / 2;
        int y = done.getY();
        openFm.setWidth(buttonWidth);
        openFm.setX(left);
        openFm.setY(y);
        done.setWidth(buttonWidth);
        done.setX(left + buttonWidth + gap);
        if (Boolean.getBoolean("m1pack.smoke")) {
            LogUtils.getLogger().info("[M1-OPTIONS-LAYOUT] {} {} {} {} {} {}",
                screen.width, screen.height, left, done.getX(), y, buttonWidth);
        }
    }
}
