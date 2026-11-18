package dev.m1ssing0.packclient;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.sounds.Music;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.client.gui.ModListScreen;

public final class PackTitleScreen extends Screen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
        "m1packclient", "textures/menu/background.png");
    private static final Music MUSIC = new Music(SoundEvents.MUSIC_MENU, 0, 0, true);
    private boolean musicObserved;

    public PackTitleScreen() {
        super(Component.literal("Лютая технологическая сборка"));
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(176, (width - 36) / 2);
        int y = Math.min(height - 80, (int) (height * 0.72));
        int left = width / 2 - buttonWidth - 4;
        int right = width / 2 + 4;
        add(left, y, buttonWidth, "menu.singleplayer", () -> minecraft.setScreen(new SelectWorldScreen(this)));
        add(right, y, buttonWidth, "menu.multiplayer", () -> minecraft.setScreen(new JoinMultiplayerScreen(this)));
        add(left, y + 25, buttonWidth, "menu.options", () -> minecraft.setScreen(new OptionsScreen(this, minecraft.options)));
        add(right, y + 25, buttonWidth, "m1pack.menu.mods", () -> minecraft.setScreen(new ModListScreen(this)));
        add(left, y + 50, buttonWidth, "m1pack.menu.sound", () -> minecraft.setScreen(new SoundOptionsScreen(this, minecraft.options)));
        add(right, y + 50, buttonWidth, "menu.quit", () -> minecraft.stop());
    }

    private void add(int x, int y, int w, String label, Runnable action) {
        addRenderableWidget(new SteelButton(x, y, w, Component.translatable(label), button -> action.run()));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xff090e13);
        // Fit the complete supplied image so its embedded title remains readable.
        double scale = Math.min(width / 1672.0, height / 941.0);
        int w = (int) Math.round(1672 * scale);
        int h = (int) Math.round(941 * scale);
        graphics.blit(BACKGROUND, (width - w) / 2, (height - h) / 2, w, h, 0, 0, 1672, 941, 1672, 941);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (Boolean.getBoolean("m1pack.smoke") && !musicObserved && minecraft.getMusicManager().isPlayingMusic(MUSIC)) {
            com.mojang.logging.LogUtils.getLogger().info("[M1-MENU] menu music is playing");
            musicObserved = true;
        }
        if (height >= 300) graphics.drawString(font, "M1 SciFi 0.3.1", 8, height - 12, 0xffc6b69d);
    }

    @Override
    public Music getBackgroundMusic() {
        return MUSIC;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private static final class SteelButton extends Button {
        private SteelButton(int x, int y, int width, Component text, OnPress action) {
            super(x, y, width, 21, text, action, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            boolean focus = isHoveredOrFocused();
            graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xe6090b0e);
            graphics.fillGradient(x, y, x + w, y + h, focus ? 0xff665044 : 0xf0394045,
                focus ? 0xff292a2c : 0xf0181d22);
            int edge = focus ? 0xffffad42 : 0xff9b7244;
            graphics.fill(x, y, x + w, y + 1, edge);
            graphics.fill(x, y + h - 1, x + w, y + h, 0xff69451d);
            graphics.fill(x, y, x + 3, y + h, focus ? 0xffffa024 : 0xffbe7d23);
            for (int rivetX : new int[]{x + 8, x + w - 9}) {
                graphics.fill(rivetX, y + 4, rivetX + 2, y + 6, 0xffa59d8b);
                graphics.fill(rivetX, y + h - 6, rivetX + 2, y + h - 4, 0xff676760);
            }
            graphics.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font,
                getMessage(), x + w / 2, y + (h - 8) / 2, focus ? 0xffffe1aa : 0xffeee4d4);
        }
    }
}
