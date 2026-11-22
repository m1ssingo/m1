package dev.m1ssing0.packclient;

import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
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
        var label = Component.translatable("m1pack.season." + current.name().toLowerCase(Locale.ROOT))
            .withStyle(ChatFormatting.BOLD);
        int width = client.font.width(label);
        float size = Math.min(4f, (g.guiWidth() - 32) / (float) Math.max(1, width));
        int alpha = Math.min(255, remaining * 255 / 15) << 24;
        var colors = switch (current) {
            case SPRING -> new int[]{0xcafd63, 0x4a6921, 0x304315};
            case SUMMER -> new int[]{0xffdc70, 0x9b5932, 0x583a22};
            case AUTUMN -> new int[]{0xffa34d, 0x923b22, 0x512618};
            case WINTER -> new int[]{0xd2efff, 0x426a91, 0x273e5c};
        };
        var pose = g.pose();
        pose.pushPose();
        pose.translate(g.guiWidth() / 2.0, Math.max(20, g.guiHeight() * 0.12), 0);
        pose.scale(size, size, 1);
        for (int y = 3; y >= 1; --y) {
            g.drawString(client.font, label, -width / 2 + 1, y, alpha | colors[2], false);
        }
        g.drawString(client.font, label, -width / 2 + 1, 1, alpha | colors[1], false);
        g.flush();
        pose.translate(0, 0, 1);
        g.drawString(client.font, label, -width / 2, 0, alpha | colors[0], false);
        pose.popPose();
    }
}
