"""Pack the menu's RGB frames into the client's streaming delta format."""
import gzip
from pathlib import Path
import struct

import numpy as np
from PIL import Image

WIDTH, HEIGHT, FRAMES, DELAY_MS = 1672, 941, 250, 80
HEADER = struct.pack('>4s4H', b'M1A1', WIDTH, HEIGHT, FRAMES, DELAY_MS)


def pack(source: Path, target: Path, fallback: Path):
    with Image.open(source) as animation:
        if (animation.size, animation.n_frames) != ((WIDTH, HEIGHT), FRAMES):
            raise ValueError('Unexpected menu animation dimensions or frame count')

        def frames():
            for index in range(FRAMES):
                animation.seek(index)
                animation.load()
                if animation.info.get('duration') != DELAY_MS:
                    raise ValueError(f'Unexpected duration for menu frame {index}')
                yield animation.convert('RGB')

        pack_frames(frames(), target, fallback)


def pack_frames(images, target: Path, fallback: Path):
    with target.open('wb') as raw, gzip.GzipFile(
        filename='', fileobj=raw, mode='wb', compresslevel=6, mtime=0
    ) as out:
        out.write(HEADER)
        previous = None
        count = 0
        for frame, image in enumerate(images):
            if frame >= FRAMES or image.size != (WIDTH, HEIGHT):
                raise ValueError(f'Unexpected menu frame {frame}')
            image = image.convert('RGB')
            if frame == 0:
                image.save(fallback)
            rgb = np.asarray(image).reshape(-1, 3)
            if previous is None:
                runs = [(0, HEIGHT * WIDTH)]
            else:
                changed = np.flatnonzero(np.any(rgb != previous, axis=1))
                runs = []
                if changed.size:
                    splits = np.flatnonzero(np.diff(changed) > 6) + 1
                    runs = zip(changed[np.r_[0, splits]], changed[np.r_[splits - 1, -1]] + 1)
            # A short gap costs less as pixels than as another run header.
            chunks = [(start, min(start + 65535, end))
                      for first, end in runs for start in range(first, end, 65535)]
            out.write(struct.pack('>I', len(chunks)))
            data = bytearray()
            for start, end in chunks:
                data.extend(struct.pack('>IH', start, end - start))
                pixels = rgb[start:end]
                if previous is not None:
                    # Byte differences wrap modulo 256, preserving the original RGB exactly.
                    pixels = pixels - previous[start:end]
                data.extend(pixels.tobytes())
            out.write(data)
            previous = rgb
            count += 1
        if count != FRAMES:
            raise ValueError(f'Expected {FRAMES} menu frames, got {count}')
