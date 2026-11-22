"""Generate the M1 interface sprites from the pinned client and JEI atlases."""

import argparse
from io import BytesIO
from pathlib import Path
from random import Random
from zipfile import ZipFile

from PIL import Image, ImageChops, ImageDraw, ImageFilter, ImageFont


RESOURCES = Path(__file__).resolve().parents[1] / 'client/resources'
VANILLA_GUI = 'assets/minecraft/textures/gui/'
JEI_GUI = 'assets/jei/textures/gui/sprites/'

ORANGE = (229, 130, 41)
ORANGE_HIGHLIGHT = (255, 160, 36)
ORANGE_RIM = (147, 74, 30)

PANEL = {
    0: (13, 11, 10),
    55: (20, 17, 15),
    76: (31, 26, 23),
    85: (73, 48, 34),
    106: (56, 44, 36),
    139: (37, 32, 29),
    153: (91, 57, 36),
    172: (114, 70, 42),
    179: (174, 101, 44),
    193: (79, 53, 39),
    198: (54, 43, 36),
    204: (94, 60, 39),
    216: (128, 72, 37),
    255: (121, 79, 52),
}
RECIPE_CARD = {**PANEL, 198: (186, 165, 142), 216: (214, 187, 151)}
CRAFT_ARROWS = {
    'container/inventory.png': (133, 27, 153, 48),
    'container/crafting_table.png': (93, 32, 116, 52),
}

JEI_PANELS = {
    'bookmark_list_background_v2', 'bookmark_list_slot_background_v2', 'catalyst_tab_v2',
    'gui_background_v2', 'ingredient_list_background_v2', 'ingredient_list_slot_background_v2',
    'interactive_ingredient_tooltip_background', 'output_slot', 'recipe_catalyst_slot_background_v2',
    'recipe_options_tab_v2', 'recipe_preview_background_v2', 'scrollbar_background_v2',
    'scrollbar_marker_v2', 'search_background_v2', 'single_recipe_background_v2', 'slot',
    'tab_selected', 'tab_unselected',
}

SEASONS = {
    'spring': ('ВЕСНА', 'SPRING', (222, 115, 160)),
    'summer': ('ЛЕТО', 'SUMMER', (104, 183, 111)),
    'autumn': ('ОСЕНЬ', 'AUTUMN', (229, 145, 70)),
    'winter': ('ЗИМА', 'WINTER', (105, 190, 228)),
}


def clamp(value):
    return max(0, min(255, round(value)))


def save(image, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path, optimize=True)


def tint_panel(original, palette, accent=False):
    image = original.copy()
    pixels = image.load()
    for y in range(image.height):
        for x in range(image.width):
            red, green, blue, alpha = pixels[x, y]
            # Keep colored controls, characters, and functional status indicators intact.
            if alpha and red == green == blue and red in palette:
                texture = ((x * 17 + y * 23) % 7 - 3) if red in (139, 198) else 0
                pixels[x, y] = tuple(clamp(value + texture) for value in palette[red]) + (alpha,)
    bounds = original.getbbox()
    if accent and bounds and bounds[2] - bounds[0] >= 50 and bounds[3] - bounds[1] >= 4:
        left, top, right, _ = bounds
        bases = {(value, value, value, 255) for value in (85, 198, 255)}
        for x in range(left + 4, right - 4):
            if original.getpixel((x, top + 2)) in bases:
                edge = min(x - left, right - 1 - x)
                pixels[x, top + 2] = (ORANGE if edge < 22 else ORANGE_RIM) + (255,)
    return image


def hotbar(original, name):
    width, height = original.size
    mask = original.getchannel('A')
    image = Image.new('RGBA', original.size)
    draw = ImageDraw.Draw(image)
    left, top, right, bottom = mask.getbbox()
    if name == 'hotbar_selection':
        draw.rectangle((0, 0, width - 1, height - 1), (22, 14, 10))
        draw.rectangle((1, 1, width - 2, height - 2), outline=ORANGE)
        draw.line((2, 2, width - 3, 2), ORANGE_HIGHLIGHT)
        draw.line((2, height - 3, width - 3, height - 3), ORANGE_RIM)
    else:
        draw.rectangle((left, top, right - 1, bottom - 1), (15, 13, 12))
        for y in range(top + 1, bottom - 1):
            shade = round(48 - 20 * (y - top) / (bottom - top))
            draw.line((left + 1, y, right - 2, y), (shade + 8, shade, shade - 5))
        draw.line((left + 2, top + 1, right - 3, top + 1), ORANGE_RIM)
        for x in range(left + 2, right - 7, 20):
            draw.rectangle((x, top + 3, x + 17, bottom - 4), fill=(32, 27, 24), outline=(18, 16, 15))
            draw.line((x + 1, bottom - 4, x + 16, bottom - 4), (102, 62, 37))
            draw.line((x + 2, bottom - 2, x + 4, bottom - 2), ORANGE)
    # Match the HUD's original transparent gaps and sprite bounds.
    image.putalpha(mask)
    return image


def inventory_sprites(vanilla, jei):
    for archive, prefix in ((vanilla, VANILLA_GUI), (jei, JEI_GUI)):
        for name in archive.namelist():
            if not name.startswith(prefix) or not name.endswith('.png'):
                continue
            relative = name.removeprefix(prefix)
            stem = Path(name).stem
            vanilla_panel = relative.startswith('container/') or relative == 'recipe_book.png'
            vanilla_recipe = relative.startswith('sprites/recipe_book/')
            vanilla_hotbar = relative.startswith('sprites/hud/') and stem in {
                'hotbar', 'hotbar_selection', 'hotbar_offhand_left', 'hotbar_offhand_right',
            }
            if archive is vanilla:
                if not (vanilla_panel or vanilla_recipe or vanilla_hotbar):
                    continue
            elif stem not in JEI_PANELS:
                continue
            original = Image.open(BytesIO(archive.read(name))).convert('RGBA')
            if archive is vanilla and vanilla_hotbar:
                updated = hotbar(original, stem)
            else:
                accent = vanilla_panel or stem.endswith('background_v2')
                # JEI integrations can draw dark labels and arrows on recipe cards.
                palette = RECIPE_CARD if stem in {'single_recipe_background_v2', 'recipe_preview_background_v2'} else PANEL
                updated = tint_panel(original, palette, accent=accent)
                if archive is vanilla and relative in CRAFT_ARROWS:
                    left, top, right, bottom = CRAFT_ARROWS[relative]
                    for y in range(top, bottom):
                        for x in range(left, right):
                            if original.getpixel((x, y)) == (139, 139, 139, 255):
                                updated.putpixel((x, y), ORANGE + (255,))
            if updated.tobytes() == original.tobytes():
                continue
            save(updated, RESOURCES / name)
            metadata = name + '.mcmeta'
            if metadata in archive.namelist():
                (RESOURCES / metadata).write_bytes(archive.read(metadata))


def offset_mask(mask, x, y):
    shifted = Image.new('L', mask.size)
    shifted.paste(mask, (x, y))
    return shifted


def season_depth(face, tint):
    mask = face.getchannel('A')
    shadow = offset_mask(mask, 5, 9).filter(ImageFilter.GaussianBlur(4.0))
    badge = Image.new('RGBA', face.size, (9, 6, 5))
    badge.putalpha(shadow.point(lambda value: value * 208 // 255))

    silhouette = ImageChops.lighter(mask, offset_mask(mask, 3, 6))
    outline = Image.new('RGBA', face.size, (22, 16, 14))
    outline.putalpha(silhouette.filter(ImageFilter.MaxFilter(5)))
    badge.alpha_composite(outline)
    for depth in range(6, 0, -1):
        side = Image.new('RGBA', face.size, tuple(clamp(10 + channel * (0.51 - depth * 0.047)) for channel in tint))
        side.putalpha(offset_mask(mask, (depth + 1) // 2, depth))
        badge.alpha_composite(side)

    badge.alpha_composite(face)
    return badge


def season_badge(label, tint, font_path):
    image = Image.new('RGBA', (512, 112))
    mask = Image.new('L', image.size)
    draw = ImageDraw.Draw(mask)
    font = ImageFont.truetype(font_path, 91)
    left, top, right, bottom = draw.textbbox((0, 0), label, font=font)
    if right - left > 472:
        font = ImageFont.truetype(font_path, int(91 * 472 / (right - left)))
        left, top, right, bottom = draw.textbbox((0, 0), label, font=font)
    draw.text(((512 - right + left) / 2 - left, (112 - bottom + top) / 2 - top),
              label, fill=255, font=font)

    face = mask.load()
    inside = mask.filter(ImageFilter.MinFilter(5)).load()
    pixels = image.load()
    left, top, right, bottom = mask.getbbox()
    rng = Random(21)
    for y in range(top, bottom):
        row = (y - top) / (bottom - top)
        polish = 25 - 49 * row + 35 * max(0, 1 - 7 * abs(row - 0.37))
        grain = rng.randrange(-12, 13)
        for x in range(left, right):
            alpha = face[x, y]
            if not alpha:
                continue
            bevel = (face[x, y + 1] - face[x, y - 1]) / 7
            bevel -= 13 * (1 - inside[x, y] / 255)
            brightness = polish + bevel + grain + rng.randrange(-6, 7)
            pixels[x, y] = tuple(clamp(channel + brightness) for channel in tint) + (alpha,)
    return season_depth(image, tint)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('minecraft_jar', type=Path)
    parser.add_argument('jei_jar', type=Path)
    parser.add_argument('russo_one_font', type=Path)
    args = parser.parse_args()
    with ZipFile(args.minecraft_jar) as vanilla, ZipFile(args.jei_jar) as jei:
        inventory_sprites(vanilla, jei)
    for season, (ru, en, tint) in SEASONS.items():
        for language, label in (('ru', ru), ('en', en)):
            badge = season_badge(label, tint, args.russo_one_font)
            save(badge, RESOURCES / f'assets/m1packclient/textures/gui/season/{language}/{season}.png')


if __name__ == '__main__':
    main()
