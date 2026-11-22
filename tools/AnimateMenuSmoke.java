import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.imageio.ImageIO;

/** Animate chimney exhaust in the same loop as the menu's original artwork. */
public final class AnimateMenuSmoke {
    private static final int MAGIC = 0x4d314131;
    private static final int PUFFS = 12;
    private static final List<Chimney> CHIMNEYS = List.of(
        new Chimney(324, 462, 8, 92, 36, 0.18, 0.16f),
        new Chimney(462, 465, 11, 111, 52, 0.69, 0.21f),
        new Chimney(528, 546, 10, 84, 42, 0.31, 0.15f),
        new Chimney(594, 619, 8, 76, 40, 0.52, 0.13f));

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            throw new IllegalArgumentException("Usage: java tools/AnimateMenuSmoke.java input.m1a smoke.png output.m1a");
        }
        Path input = Path.of(args[0]).toAbsolutePath().normalize();
        Path output = Path.of(args[2]).toAbsolutePath().normalize();
        if (input.equals(output) || !output.toString().endsWith(".m1a")) {
            throw new IllegalArgumentException("Expected a separate .m1a output");
        }
        var smoke = ImageIO.read(Path.of(args[1]).toFile());
        if (smoke == null || !smoke.getColorModel().hasAlpha()) throw new IOException("Expected an RGBA smoke texture");
        String file = output.getFileName().toString();
        Path still = output.resolveSibling(file.substring(0, file.length() - 4) + ".png");
        try (var source = new DataInputStream(new BufferedInputStream(new GZIPInputStream(Files.newInputStream(input))));
             var target = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(Files.newOutputStream(output),
                 32768)))) {
            if (source.readInt() != MAGIC) throw new IOException("Unexpected animation header");
            int width = source.readUnsignedShort(), height = source.readUnsignedShort();
            int frames = source.readUnsignedShort(), delay = source.readUnsignedShort();
            if (width != 1672 || height != 941 || frames != 250 || delay != 80) {
                throw new IOException("Unexpected menu animation size or timing");
            }
            target.writeInt(MAGIC);
            target.writeShort(width);
            target.writeShort(height);
            target.writeShort(frames);
            target.writeShort(delay);

            int[] scene = new int[width * height];
            int[] last = new int[scene.length];
            var canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int[] paint = ((DataBufferInt) canvas.getRaster().getDataBuffer()).getData();
            Graphics2D g = canvas.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            byte[] delta = new byte[3 * 65535];
            try {
                for (int frame = 0; frame < frames; frame++) {
                    addFrame(source, scene, delta);
                    System.arraycopy(scene, 0, paint, 0, scene.length);
                    for (var chimney : CHIMNEYS) chimney.draw(g, smoke, 2.0 * frame / frames, width);
                    if (frame == 0) ImageIO.write(canvas, "png", still.toFile());
                    writeFrame(target, paint, last, delta, frame == 0);
                    System.arraycopy(paint, 0, last, 0, scene.length);
                }
            } finally {
                g.dispose();
            }
            if (source.read() != -1) throw new IOException("Unexpected animation trailer");
        }
    }

    private static void addFrame(DataInputStream source, int[] scene, byte[] delta) throws IOException {
        int runs = source.readInt();
        if (runs < 0 || runs > scene.length) throw new IOException("Invalid animation run count");
        for (int run = 0; run < runs; run++) {
            int start = source.readInt();
            int length = source.readUnsignedShort();
            if (start < 0 || length == 0 || start > scene.length - length) throw new IOException("Invalid animation run");
            source.readFully(delta, 0, length * 3);
            for (int i = 0; i < length; i++) {
                int rgb = scene[start + i];
                int red = ((rgb >>> 16) + delta[3 * i]) & 255;
                int green = ((rgb >>> 8) + delta[3 * i + 1]) & 255;
                int blue = (rgb + delta[3 * i + 2]) & 255;
                scene[start + i] = red << 16 | green << 8 | blue;
            }
        }
    }

    private static void writeFrame(DataOutputStream target, int[] paint, int[] last, byte[] delta, boolean first)
        throws IOException {
        var runs = new ArrayList<Run>();
        int pixel = 0;
        while (pixel < paint.length) {
            if (!first && ((paint[pixel] ^ last[pixel]) & 0xffffff) == 0) {
                ++pixel;
                continue;
            }
            int start = pixel++;
            int end = pixel;
            while (pixel < paint.length && pixel < start + 65535 && pixel < end + 6) {
                if (first || ((paint[pixel] ^ last[pixel]) & 0xffffff) != 0) end = pixel + 1;
                ++pixel;
            }
            runs.add(new Run(start, end - start));
        }
        target.writeInt(runs.size());
        for (var run : runs) {
            target.writeInt(run.start);
            target.writeShort(run.size);
            for (int i = 0; i < run.size; ++i) {
                int current = paint[run.start + i], previous = last[run.start + i];
                delta[3 * i] = (byte) ((current >>> 16) - (previous >>> 16));
                delta[3 * i + 1] = (byte) ((current >>> 8) - (previous >>> 8));
                delta[3 * i + 2] = (byte) (current - previous);
            }
            target.write(delta, 0, run.size * 3);
        }
    }

    private record Run(int start, int size) {}

    private record Chimney(int x, int y, int mouth, int rise, int drift, double offset, float opacity) {
        void draw(Graphics2D g, BufferedImage smoke, double time, int width) {
            g.setClip(0, 0, width, y + 1);
            for (int puff = PUFFS - 1; puff >= 0; --puff) {
                double cycle = time + offset + (double) puff / PUFFS;
                double age = cycle - Math.floor(cycle);
                double wave = Math.sin((age + offset) * Math.PI * 3);
                int size = (int) (mouth + 55 * age);
                int thick = Math.max(2, (int) (size * (0.73 + 0.08 * wave)));
                int cx = (int) (x - drift * age * age + 7 * wave * age);
                int bottom = (int) (y - rise * age);
                float fade = (float) (Math.min(1, age * 25) * Math.pow(1 - age, 0.65));
                g.setComposite(AlphaComposite.SrcOver.derive(fade * opacity));
                g.drawImage(smoke, cx - size / 2, bottom - thick, size, thick, null);
            }
        }
    }
}
