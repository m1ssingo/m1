package dev.m1ssing0.packclient;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

final class MenuAnimation {
    static final int WIDTH = 1672;
    static final int HEIGHT = 941;
    private static final int PIXELS = WIDTH * HEIGHT;
    private static final int MAGIC = 0x4d314131; // M1A1
    private static final ResourceLocation ASSET = ResourceLocation.fromNamespaceAndPath(
        "m1packclient", "textures/menu/background.m1a");

    private final Minecraft minecraft;
    private final Resource resource;
    private final byte[] rgb = new byte[65535 * 3];
    private DynamicTexture texture;
    private ResourceLocation location;
    private DataInputStream input;
    private int frameCount;
    private int frameIndex;
    private long frameTime;
    private long nextFrame;

    MenuAnimation(Minecraft minecraft, Resource resource) {
        this.minecraft = minecraft;
        this.resource = resource;
    }

    static MenuAnimation find(Minecraft minecraft) {
        return minecraft.getResourceManager().getResource(ASSET)
            .map(resource -> new MenuAnimation(minecraft, resource)).orElse(null);
    }

    ResourceLocation advance() throws IOException {
        long now = System.nanoTime();
        if (texture == null) {
            openLoop();
            texture = new DynamicTexture(WIDTH, HEIGHT, false);
            readFrame(texture.getPixels());
            upload();
            location = minecraft.getTextureManager().register("m1pack_menu_animation", texture);
            nextFrame = System.nanoTime() + frameTime;
            if (Boolean.getBoolean("m1pack.smoke")) {
                LogUtils.getLogger().info("[M1-ANIMATION] {}x{} {} frames every {}ms",
                    WIDTH, HEIGHT, frameCount, TimeUnit.NANOSECONDS.toMillis(frameTime));
            }
        } else if (now >= nextFrame) {
            if (frameIndex == frameCount) {
                openLoop();
                if (Boolean.getBoolean("m1pack.smoke")) LogUtils.getLogger().info("[M1-ANIMATION] loop");
            }
            readFrame(texture.getPixels());
            upload();
            // Bound work per render even if the client stalls or loses focus.
            nextFrame = Math.max(nextFrame + frameTime, now + frameTime / 2);
        }
        return location;
    }

    private void upload() {
        texture.bind();
        // Keep linear filtering for every upload so resized windows sample the same way across frames.
        texture.getPixels().upload(0, 0, 0, 0, 0, WIDTH, HEIGHT, true, true, false, false);
    }

    private void openLoop() throws IOException {
        if (input != null) input.close();
        try {
            InputStream source = resource.open();
            try {
                input = new DataInputStream(new BufferedInputStream(new GZIPInputStream(source)));
            } catch (IOException e) {
                source.close();
                throw e;
            }
            if (input.readInt() != MAGIC || input.readUnsignedShort() != WIDTH
                || input.readUnsignedShort() != HEIGHT) {
                throw new IOException("Invalid menu animation dimensions or format");
            }
            frameCount = input.readUnsignedShort();
            frameTime = TimeUnit.MILLISECONDS.toNanos(input.readUnsignedShort());
            if (frameCount == 0 || frameTime == 0) throw new IOException("Invalid menu animation timing");
            frameIndex = 0;
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    private void readFrame(NativeImage image) throws IOException {
        int runs = input.readInt();
        if (runs < 0 || runs > PIXELS) throw new IOException("Invalid menu animation run count");
        int end = 0;
        boolean keyframe = frameIndex == 0;
        for (int i = 0; i < runs; i++) {
            int start = input.readInt();
            int length = input.readUnsignedShort();
            if (start < end || start > PIXELS - length || length == 0
                || (keyframe && start != end)) {
                throw new IOException("Invalid menu animation pixel range");
            }
            input.readFully(rgb, 0, length * 3);
            end = start + length;
            for (int pixel = start, offset = 0; pixel < end; pixel++, offset += 3) {
                int x = pixel % WIDTH;
                int y = pixel / WIDTH;
                int previous = keyframe ? 0 : image.getPixelRGBA(x, y);
                int red = (previous + rgb[offset]) & 255;
                int green = ((previous >>> 8) + rgb[offset + 1]) & 255;
                int blue = ((previous >>> 16) + rgb[offset + 2]) & 255;
                image.setPixelRGBA(x, y, 0xff000000 | blue << 16 | green << 8 | red);
            }
        }
        if (keyframe && end != PIXELS) throw new IOException("Incomplete menu animation keyframe");
        frameIndex++;
    }

    void close() {
        if (location != null) {
            minecraft.getTextureManager().release(location);
            location = null;
        } else if (texture != null) {
            texture.close();
        }
        texture = null;
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                LogUtils.getLogger().warn("Could not close the menu animation", e);
            }
            input = null;
        }
    }
}
