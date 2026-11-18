import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import me.michiyo.openfm.client.audio.AacDecoder;

/** Exercises the decoder embedded in the actual selected OpenFM release. */
public final class PcmProbe {
    public static void main(String[] args) throws Exception {
        int total = 0;
        try (InputStream input = Files.newInputStream(Path.of(args[0]))) {
            AacDecoder decoder = new AacDecoder(input);
            try {
                for (int i = 0; i < 8; i++) {
                    byte[] pcm = decoder.readPcmChunk(8192);
                    if (pcm == null) break;
                    total += pcm.length;
                }
                System.out.println("{\"sample_rate\":" + decoder.getSampleRate()
                    + ",\"channels\":" + decoder.getChannels()
                    + ",\"pcm_bytes\":" + total + "}");
            } finally {
                decoder.close();
            }
        }
        if (total == 0) throw new IllegalStateException("Decoder produced no PCM samples");
    }
}
