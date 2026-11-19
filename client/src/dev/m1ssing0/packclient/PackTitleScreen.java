package dev.m1ssing0.packclient;

import java.io.IOException;
import com.mojang.logging.LogUtils;
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

    @Override
    protected void init() {
        artwork = Artwork.fit(width, height);
        // Keep controls below the byline, between the two foreground characters.
        int gap = 8;
        int rowGap = 4;
        int buttonWidth = Math.min(140, (Math.round(artwork.width() * 0.44f) - gap) / 2);
        int y = artwork.y() + Math.round(artwork.height() * 0.68f);
        int bottom = artwork.y() + Math.round(artwork.height() * 0.96f);
        int buttonHeight = Math.min(21, (bottom - y - 2 * rowGap) / 3);
        int pitch = buttonHeight + rowGap;
        int left = (width - 2 * buttonWidth - gap) / 2;
        int right = left + buttonWidth + gap;
        add(left, y, buttonWidth, buttonHeight, "menu.singleplayer",
            () -> minecraft.setScreen(new SelectWorldScreen(this)));
        add(right, y, buttonWidth, buttonHeight, "menu.multiplayer",
            () -> minecraft.setScreen(new JoinMultiplayerScreen(this)));
        add(left, y + pitch, buttonWidth, buttonHeight, "menu.options",
            () -> minecraft.setScreen(new OptionsScreen(this, minecraft.options)));
        add(right, y + pitch, buttonWidth, buttonHeight, "m1pack.menu.mods",
            () -> minecraft.setScreen(new ModListScreen(this)));
        add(left, y + 2 * pitch, buttonWidth, buttonHeight, "m1pack.menu.sound",
            () -> minecraft.setScreen(new SoundOptionsScreen(this, minecraft.options)));
        add(right, y + 2 * pitch, buttonWidth, buttonHeight, "menu.quit", () -> minecraft.stop());
        if (Boolean.getBoolean("m1pack.smoke")) {
            LogUtils.getLogger().info("[M1-MENU-LAYOUT] {} {} {} {} {} {} {} {} {} {} {} {}",
                width, height, artwork.x(), artwork.y(), artwork.width(), artwork.height(),
                left, right, y, buttonWidth, buttonHeight, pitch);
        }
    }

    private void add(int x, int y, int w, int h, String label, Runnable action) {
        addRenderableWidget(new SteelButton(x, y, w, h, Component.translatable(label), button -> action.run()));
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
        graphics.fill(0, 0, width, height, 0xff090e13);
        graphics.blit(background, artwork.x(), artwork.y(), artwork.width(), artwork.height(),
            0, 0, MenuAnimation.WIDTH, MenuAnimation.HEIGHT, MenuAnimation.WIDTH, MenuAnimation.HEIGHT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (Boolean.getBoolean("m1pack.smoke") && !musicObserved && minecraft.getMusicManager().isPlayingMusic(MUSIC)) {
            LogUtils.getLogger().info("[M1-MENU] menu music is playing");
            musicObserved = true;
        }
        if (height >= 300) graphics.drawString(font, "M1 SciFi 0.7.0", 8, height - 12, 0xffc6b69d);
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

    private record Artwork(int x, int y, int width, int height) {
        static Artwork fit(int width, int height) {
            double scale = Math.min((double) width / MenuAnimation.WIDTH, (double) height / MenuAnimation.HEIGHT);
            int fitWidth = (int) Math.round(MenuAnimation.WIDTH * scale);
            int fitHeight = (int) Math.round(MenuAnimation.HEIGHT * scale);
            return new Artwork((width - fitWidth) / 2, (height - fitHeight) / 2, fitWidth, fitHeight);
        }
    }

    private static final class SteelButton extends Button {
        private SteelButton(int x, int y, int width, int height, Component text, OnPress action) {
            super(x, y, width, height, text, action, DEFAULT_NARRATION);
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
            var font = Minecraft.getInstance().font;
            int textWidth = font.width(getMessage());
            float scale = Math.min(1.0f, (w - 22) / (float) Math.max(1, textWidth));
            var pose = graphics.pose();
            pose.pushPose();
            pose.translate(x + w / 2.0f, y + h / 2.0f, 0);
            pose.scale(scale, scale, 1);
            graphics.drawCenteredString(font, getMessage(), 0, -4, focus ? 0xffffe1aa : 0xffeee4d4);
            pose.popPose();
        }
    }
}
