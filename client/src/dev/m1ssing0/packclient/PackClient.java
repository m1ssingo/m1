package dev.m1ssing0.packclient;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;

@Mod(value = "m1packclient", dist = Dist.CLIENT)
@EventBusSubscriber(modid = "m1packclient", value = Dist.CLIENT)
public final class PackClient {
    private static boolean audited;

    @SubscribeEvent
    public static void openScreen(ScreenEvent.Opening event) {
        if (Boolean.getBoolean("m1pack.smoke") && event.getNewScreen() != null) {
            com.mojang.logging.LogUtils.getLogger().info("[M1-SCREEN] {}", event.getNewScreen().getClass().getSimpleName());
        }
        if (event.getNewScreen() instanceof TitleScreen) {
            if (!audited) {
                KeyAudit.apply(Minecraft.getInstance());
                audited = true;
            }
            event.setNewScreen(new PackTitleScreen());
        }
    }
}
