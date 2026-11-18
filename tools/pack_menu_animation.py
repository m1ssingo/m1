"""Pack the approved lossless WebP into the client's streaming delta format."""
import gzip
import hashlib
from pathlib import Path
import struct

import numpy as np
from PIL import Image

SOURCE_SHA256 = '1f5976c9fa074c84a600327a0005fbba1fee3a436e89ec3e5f8d067cda65ecb9'
WIDTH, HEIGHT, FRAMES, DELAY_MS = 1672, 941, 200, 80


def pack(source: Path, target: Path, fallback: Path):
    with source.open('rb') as stream:
        if hashlib.file_digest(stream, 'sha256').hexdigest() != SOURCE_SHA256:
            raise ValueError('Expected the approved M1-menu-animation-v2.webp')
    with Image.open(source) as animation, target.open('wb') as raw, gzip.GzipFile(
        filename='', fileobj=raw, mode='wb', compresslevel=6, mtime=0
    ) as out:
        if (animation.size, animation.n_frames) != ((WIDTH, HEIGHT), FRAMES):
            raise ValueError('Unexpected menu animation dimensions or frame count')
        out.write(struct.pack('>4s4H', b'M1A1', WIDTH, HEIGHT, FRAMES, DELAY_MS))
        previous = None
        for frame in range(FRAMES):
            animation.seek(frame)
            animation.load()
            if animation.info['duration'] != DELAY_MS:
                raise ValueError(f'Unexpected duration for menu frame {frame}')
            image = animation.convert('RGB')
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
