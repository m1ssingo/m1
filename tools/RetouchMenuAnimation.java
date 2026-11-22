import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.imageio.ImageIO;

/** Bake a localized retouch into the original still and lossless animation. */
public final class RetouchMenuAnimation {
    private static final int MAGIC = 0x4d314131;

    public static void main(String[] args) throws IOException {
        if (args.length != 5) {
            throw new IllegalArgumentException(
                "Usage: java tools/RetouchMenuAnimation.java original.m1a retouch.png x y output.m1a");
        }
        Path source = Path.of(args[0]).toAbsolutePath().normalize();
        Path output = Path.of(args[4]).toAbsolutePath().normalize();
        if (source.equals(output)) {
            throw new IllegalArgumentException("The output must be a separate .m1a file");
        }

        BufferedImage still = ImageIO.read(fallback(source).toFile());
        BufferedImage replacement = ImageIO.read(Path.of(args[1]).toFile());
        if (still == null || replacement == null || !replacement.getColorModel().hasAlpha()) {
            throw new IOException("Expected the original still and an RGBA retouch");
        }

        int x = Integer.parseInt(args[2]);
        int y = Integer.parseInt(args[3]);
        int width = replacement.getWidth();
        int height = replacement.getHeight();
        if (x < 0 || y < 0 || x > still.getWidth() - width || y > still.getHeight() - height) {
            throw new IOException("Retouch is outside the animation");
        }

        int[] changes = new int[3 * width * height];
        int[] reference = new int[changes.length];
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                int paint = replacement.getRGB(column, row);
                int alpha = paint >>> 24;
                int oldPixel = still.getRGB(x + column, y + row);
                int newPixel = 0xff000000;
                int index = 3 * (row * width + column);
                for (int channel = 0; channel < 3; channel++) {
                    int shift = 16 - 8 * channel;
                    int old = oldPixel >>> shift & 255;
                    int edited = paint >>> shift & 255;
                    int blended = (old * (255 - alpha) + edited * alpha + 127) / 255;
                    reference[index + channel] = old;
                    changes[index + channel] = blended - old;
                    newPixel |= blended << shift;
                }
                still.setRGB(x + column, y + row, newPixel);
            }
        }

        var retouch = new Retouch(x, y, width, height, changes, reference, new int[changes.length]);
        rewrite(source, output, still.getWidth(), still.getHeight(), retouch);
        ImageIO.write(still, "png", fallback(output).toFile());
    }

    private static Path fallback(Path animation) {
        String name = animation.getFileName().toString();
        if (!name.endsWith(".m1a")) throw new IllegalArgumentException("Expected an .m1a file");
        return animation.resolveSibling(name.substring(0, name.length() - 4) + ".png");
    }

    private static void rewrite(Path source, Path output, int width, int height, Retouch retouch) throws IOException {
        try (var input = new DataInputStream(new BufferedInputStream(new GZIPInputStream(Files.newInputStream(source))));
             var data = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(Files.newOutputStream(output),
                 32768)))) {
            if (input.readInt() != MAGIC || input.readUnsignedShort() != width || input.readUnsignedShort() != height) {
                throw new IOException("Animation dimensions differ from the original still");
            }
            int frames = input.readUnsignedShort();
            int delay = input.readUnsignedShort();
            if (frames < 1 || delay < 1) throw new IOException("Invalid animation timing");
            data.writeInt(MAGIC);
            data.writeShort(width);
            data.writeShort(height);
            data.writeShort(frames);
            data.writeShort(delay);

            // The input already covers every pixel that can change. Keep its run boundaries.
            byte[] delta = new byte[3 * 65535];
            int pixels = width * height;
            for (int frame = 0; frame < frames; frame++) {
                int runs = input.readInt();
                if (runs < 0 || runs > pixels) throw new IOException("Invalid animation run count");
                data.writeInt(runs);
                int previousEnd = 0;
                for (int run = 0; run < runs; run++) {
                    int start = input.readInt();
                    int length = input.readUnsignedShort();
                    if (length == 0 || start < previousEnd || start > pixels - length
                        || frame == 0 && start != previousEnd) {
                        throw new IOException("Invalid animation run at frame " + frame);
                    }
                    input.readFully(delta, 0, 3 * length);
                    retouch.apply(delta, start, start + length, width, frame == 0);
                    data.writeInt(start);
                    data.writeShort(length);
                    data.write(delta, 0, 3 * length);
                    previousEnd = start + length;
                }
                if (frame == 0 && previousEnd != pixels) throw new IOException("Incomplete first frame");
            }
            if (input.read() != -1) throw new IOException("Unexpected trailing animation data");
        }
    }

    private record Retouch(int x, int y, int width, int height, int[] changes, int[] reference, int[] original) {
        void apply(byte[] delta, int start, int end, int frameWidth, boolean firstFrame) throws IOException {
            int top = Math.max(y, start / frameWidth);
            int bottom = Math.min(y + height - 1, (end - 1) / frameWidth);
            for (int row = top; row <= bottom; row++) {
                int rowStart = row * frameWidth + x;
                int left = Math.max(start, rowStart);
                int right = Math.min(end, rowStart + width);
                int offset = 3 * ((row - y) * width + left - rowStart);
                for (int index = 3 * (left - start); index < 3 * (right - start); index++, offset++) {
                    int before = original[offset];
                    int after = (before + delta[index]) & 255;
                    original[offset] = after;
                    if (firstFrame && after != reference[offset]) {
                        throw new IOException("The still and the first animation frame have different pixels");
                    }
                    // Add the same correction to each frame so the original light motion carries through.
                    int correction = changes[offset];
                    int previous = firstFrame ? 0 : Math.clamp(before + correction, 0, 255);
                    delta[index] = (byte) (Math.clamp(after + correction, 0, 255) - previous);
                }
            }
        }
    }
}
