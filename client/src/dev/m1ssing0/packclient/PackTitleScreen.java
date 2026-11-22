package dev.m1ssing0.packclient;

import java.io.IOException;
import java.util.List;
import com.mojang.logging.LogUtils;
import dev.m1ssing0.packclient.SteelButtonStyle.State;
import net.minecraft.client.Minecraft;
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
    private MenuAnimation animation;
    private Artwork artwork;
    private boolean musicObserved;
    private boolean animationChecked;

    public PackTitleScreen() {
        super(Component.literal("Лютая технологическая сборка"));
    }

    public static void drawSubmenuBackdrop(GuiGraphics graphics, int screenWidth, int screenHeight) {
        Artwork.cover(screenWidth, screenHeight).draw(graphics, BACKGROUND);
        graphics.flush();
    }

    @Override
    protected void init() {
        artwork = Artwork.cover(width, height);
        // Keep controls below the byline, between the two foreground characters.
        int gap = 8;
        int rowGap = 4;
        int margin = 6;
        int spaceBelowByline = height - margin - Math.round(artwork.y() + artwork.height() * 0.64f);
        int rows = spaceBelowByline >= 71 ? 3 : spaceBelowByline >= 46 ? 2 : 1;
        int columns = 6 / rows;
        int span = Math.min(width - 2 * margin, Math.round(artwork.width() * (columns == 2 ? 0.44f : 0.66f)));
        int buttonWidth = Math.min(140, (span - (columns - 1) * gap) / columns);
        int buttonHeight = Math.min(21, (height - 2 * margin - (rows - 1) * rowGap) / rows);
        int lastY = height - margin - rows * buttonHeight - (rows - 1) * rowGap;
        int y = Math.clamp(Math.round(artwork.y() + artwork.height() * 0.68f), margin, lastY);
        int pitch = buttonHeight + rowGap;
        int groupWidth = columns * buttonWidth + (columns - 1) * gap;
        int left = Math.clamp(Math.round(artwork.x() + (artwork.width() - groupWidth) / 2), margin,
            width - margin - groupWidth);
        var entries = List.of(
            new MenuEntry("menu.singleplayer", () -> minecraft.setScreen(new SelectWorldScreen(this))),
            new MenuEntry("menu.multiplayer", () -> minecraft.setScreen(new JoinMultiplayerScreen(this))),
            new MenuEntry("menu.options", () -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))),
            new MenuEntry("m1pack.menu.mods", () -> minecraft.setScreen(new ModListScreen(this))),
            new MenuEntry("m1pack.menu.sound", () -> minecraft.setScreen(new SoundOptionsScreen(this, minecraft.options))),
            new MenuEntry("menu.quit", () -> minecraft.stop()));
        for (int index = 0; index < entries.size(); ++index) {
            int x = left + (index % columns) * (buttonWidth + gap);
            int top = y + (index / columns) * pitch;
            var entry = entries.get(index);
            addRenderableWidget(new SteelButton(x, top, buttonWidth, buttonHeight, Component.translatable(entry.label()),
                button -> entry.action().run()));
        }
        if (Boolean.getBoolean("m1pack.smoke")) {
            LogUtils.getLogger().info("[M1-MENU-LAYOUT] {} {} {} {} {} {} {} {} {} {} {} {}",
                width, height, Math.round(artwork.x()), Math.round(artwork.y()), Math.round(artwork.width()),
                Math.round(artwork.height()),
                left, left + buttonWidth + gap, y, buttonWidth, buttonHeight, pitch);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!animationChecked) {
            animation = MenuAnimation.find(minecraft);
            animationChecked = true;
        }
        ResourceLocation background = BACKGROUND;
        if (animation != null) {
            try {
                background = animation.advance();
            } catch (IOException e) {
                LogUtils.getLogger().warn("Could not play the menu animation", e);
                animation.close();
                animation = null;
            }
        }
        artwork.draw(graphics, background);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (Boolean.getBoolean("m1pack.smoke") && !musicObserved && minecraft.getMusicManager().isPlayingMusic(MUSIC)) {
            LogUtils.getLogger().info("[M1-MENU] menu music is playing");
            musicObserved = true;
        }
        if (height >= 300) graphics.drawString(font, "M1 SciFi 0.10.2", 8, height - 12, 0xffc6b69d);
    }

    @Override
    public void removed() {
        if (animation != null) {
            animation.close();
            animation = null;
        }
        animationChecked = false;
        super.removed();
    }

    @Override
    public Music getBackgroundMusic() {
        return MUSIC;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private record MenuEntry(String label, Runnable action) {}

    private record Artwork(float x, float y, float scale) {
        static Artwork cover(int width, int height) {
            float scale = Math.max((float) width / MenuAnimation.WIDTH, (float) height / MenuAnimation.HEIGHT);
            float x = (width - MenuAnimation.WIDTH * scale) / 2;
            float y = (height - MenuAnimation.HEIGHT * scale) / 2;
            if (width / scale >= 1100) {
                // Keep the rocket visible as the sides are cropped in taller windows.
                x = Math.min(x, width - 1540 * scale);
            }
            if (height / scale >= 450) {
                y = Math.max(y, -183 * scale);
            }
            return new Artwork(x, y, scale);
        }

        float width() {
            return MenuAnimation.WIDTH * scale;
        }

        float height() {
            return MenuAnimation.HEIGHT * scale;
        }

        void draw(GuiGraphics graphics, ResourceLocation background) {
            var pose = graphics.pose();
            pose.pushPose();
            pose.translate(x, y, 0);
            pose.scale(scale, scale, 1);
            graphics.blit(background, 0, 0, 0, 0, MenuAnimation.WIDTH, MenuAnimation.HEIGHT,
                MenuAnimation.WIDTH, MenuAnimation.HEIGHT);
            pose.popPose();
        }
    }

    private static final class SteelButton extends Button {
        private SteelButton(int x, int y, int width, int height, Component text, OnPress action) {
            super(x, y, width, height, text, action, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            State state = State.of(active, isHoveredOrFocused());
            SteelButtonStyle.draw(graphics, x - 1, y - 1, w + 2, h + 2, state);
            var font = Minecraft.getInstance().font;
            int textWidth = font.width(getMessage());
            float scale = Math.min(1.0f, Math.max(1, w - 22) / (float) Math.max(1, textWidth));
            var pose = graphics.pose();
            pose.pushPose();
            pose.translate(x + w / 2.0f, y + h / 2.0f, 0);
            pose.scale(scale, scale, 1);
            graphics.drawCenteredString(font, getMessage(), 0, -4, state.textColor());
            pose.popPose();
        }
    }
}
