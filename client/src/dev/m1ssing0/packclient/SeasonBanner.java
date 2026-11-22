package dev.m1ssing0.packclient;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.season.SeasonHandlerClient;

@EventBusSubscriber(modid = "m1packclient", value = Dist.CLIENT)
public final class SeasonBanner {
    private static final int DURATION = 80;
    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 112;
    private static ClientLevel world;
    private static int joinDelay;
    private static Season current;
    private static int remaining;

    private SeasonBanner() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var client = Minecraft.getInstance();
        var level = client.level;
        if (level != world) {
            world = level;
            current = null;
            joinDelay = 20;
            remaining = 0;
        }
        if (level == null || !Level.OVERWORLD.equals(level.dimension()) || client.player == null
            || client.screen != null || !SeasonHandlerClient.clientSeasonCycleTicks.containsKey(level.dimension())) return;
        if (joinDelay > 0) {
            --joinDelay;
            return;
        }

        var observed = SeasonHelper.getSeasonState(level).getSeason();
        if (observed != current) {
            current = observed;
            remaining = DURATION;
        } else if (remaining > 0) {
            --remaining;
        }
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        var client = Minecraft.getInstance();
        if (remaining <= 0 || current == null || client.level != world || client.options.hideGui
            || client.screen != null) return;

        var g = event.getGuiGraphics();
        var language = client.getLanguageManager().getSelected();
        var locale = "ru_ru".equals(language) ? "ru" : "en";
        var name = current.name().toLowerCase(Locale.ROOT);
        var texture = ResourceLocation.fromNamespaceAndPath("m1packclient",
            "textures/gui/season/" + locale + "/" + name + ".png");
        int width = Math.min(234, Math.max(1, g.guiWidth() - 24));
        int height = Math.max(1, width * TEXTURE_HEIGHT / TEXTURE_WIDTH);
        int x = (g.guiWidth() - width) / 2;
        int y = Math.max(12, (int) (g.guiHeight() * 0.105));
        float alpha = Math.min(1f, remaining / 15f);

        RenderSystem.enableBlend();
        g.setColor(1f, 1f, 1f, alpha);
        try {
            g.blit(texture, x, y, width, height, 0f, 0f, TEXTURE_WIDTH, TEXTURE_HEIGHT,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);
        } finally {
            g.setColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
        }
    }
}
