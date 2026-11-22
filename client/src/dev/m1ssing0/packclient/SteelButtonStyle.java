package dev.m1ssing0.packclient;

import net.minecraft.client.gui.GuiGraphics;

public final class SteelButtonStyle {
    private SteelButtonStyle() {}

    public enum State {
        NORMAL(0xf0394045, 0xf0181d22, 0xff9b7244, 0xffbe7d23, 0xffeee4d4),
        HIGHLIGHTED(0xff665044, 0xff292a2c, 0xffffad42, 0xffffa024, 0xffffe1aa),
        DISABLED(0xf02d3135, 0xf01c2024, 0xff555456, 0xff57504a, 0xffa0a0a0);

        private final int top;
        private final int bottom;
        private final int rim;
        private final int stripe;
        private final int text;

        State(int top, int bottom, int rim, int stripe, int text) {
            this.top = top;
            this.bottom = bottom;
            this.rim = rim;
            this.stripe = stripe;
            this.text = text;
        }

        public static State of(boolean active, boolean highlighted) {
            if (!active) return DISABLED;
            return highlighted ? HIGHLIGHTED : NORMAL;
        }

        public int textColor() {
            return text;
        }
    }

    public static void draw(GuiGraphics graphics, int x, int y, int width, int height, State state) {
        if (width < 3 || height < 3) return;

        int right = x + width - 1;
        int bottom = y + height - 1;
        graphics.fill(x, y, right + 1, bottom + 1, 0xe6090b0e);
        ++x;
        ++y;
        graphics.fillGradient(x, y, right, bottom, state.top, state.bottom);
        graphics.fill(x, y, right, y + 1, state.rim);
        graphics.fill(x, bottom - 1, right, bottom, state == State.DISABLED ? 0xff49413b : 0xff69451d);
        int stripeWidth = Math.min(3, Math.max(1, width / 8));
        graphics.fill(x, y, x + stripeWidth, bottom, state.stripe);
        if (width >= 32 && height >= 15 && state != State.DISABLED) {
            drawRivets(graphics, x + 8, y, bottom);
            drawRivets(graphics, right - 9, y, bottom);
        }
    }

    private static void drawRivets(GuiGraphics graphics, int x, int top, int bottom) {
        graphics.fill(x, top + 4, x + 2, top + 6, 0xffa59d8b);
        graphics.fill(x, bottom - 6, x + 2, bottom - 4, 0xff676760);
    }

    public static void drawSliderHandle(GuiGraphics graphics, int x, int y, int width, int height, boolean focused) {
        int rim = focused ? 0xffffd181 : 0xffba945f;
        int bottom = y + height - 1;
        graphics.fill(x, bottom - 5, x + width, bottom, 0xff171a1d);
        graphics.fillGradient(x + 1, bottom - 4, x + width - 1, bottom, rim, 0xff715033);
    }

    public enum Icon {
        LOCKED, UNLOCKED, CLOSE, CHECK, BACK, FORWARD
    }

    public static void drawIcon(GuiGraphics graphics, int x, int y, int width, int height, State state, Icon icon) {
        draw(graphics, x, y, width, height, state);
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int color = state.textColor();
        switch (icon) {
            case LOCKED, UNLOCKED -> {
                int lock = icon == Icon.LOCKED ? centerX : centerX + 2;
                graphics.fill(lock - 2, centerY - 5, lock + 3, centerY - 4, color);
                graphics.fill(lock + 2, centerY - 4, lock + 3, centerY, color);
                if (icon == Icon.LOCKED) graphics.fill(lock - 2, centerY - 4, lock - 1, centerY, color);
                graphics.fill(centerX - 3, centerY, centerX + 4, centerY + 5, color);
                graphics.fill(centerX, centerY + 1, centerX + 1, centerY + 4, 0xff282b2e);
            }
            case CLOSE -> {
                for (int offset = -3; offset <= 3; ++offset) {
                    graphics.fill(centerX + offset, centerY + offset, centerX + offset + 1, centerY + offset + 1, color);
                    graphics.fill(centerX - offset, centerY + offset, centerX - offset + 1, centerY + offset + 1, color);
                }
            }
            case CHECK -> {
                for (int offset = 0; offset < 3; ++offset) {
                    graphics.fill(centerX - 4 + offset, centerY + offset, centerX - 3 + offset, centerY + offset + 2, color);
                }
                for (int offset = 0; offset < 6; ++offset) {
                    graphics.fill(centerX - 1 + offset, centerY + 2 - offset, centerX + offset, centerY + 4 - offset, color);
                }
            }
            case BACK, FORWARD -> {
                int direction = icon == Icon.BACK ? -1 : 1;
                for (int offset = 0; offset < 4; ++offset) {
                    int tip = centerX + offset * direction - direction;
                    graphics.fill(tip, centerY - 3 + offset, tip + 1, centerY + 4 - offset, color);
                }
                graphics.fill(centerX - 3, centerY, centerX + 4, centerY + 1, color);
            }
        }
    }
}
