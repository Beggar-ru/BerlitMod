from PIL import Image, ImageDraw

DARK = (22, 25, 29, 255)
EDGE = (10, 11, 13, 255)
MID = (52, 60, 67, 255)
LIGHT = (105, 118, 128, 255)
CYAN = (79, 214, 205, 255)
RED = (214, 62, 62, 255)

STORAGE = [
    ("crafting_storage_256k", (78, 156, 220)),
    ("crafting_storage_1m", (78, 196, 170)),
    ("crafting_storage_4m", (132, 196, 78)),
    ("crafting_storage_16m", (212, 172, 74)),
    ("crafting_storage_32m", (222, 132, 74)),
    ("crafting_storage_64m", (214, 62, 62)),
    ("crafting_storage_128m", (178, 92, 204)),
    ("crafting_storage_256m", (112, 112, 240)),
    ("crafting_storage_512m", (79, 214, 205)),
]

PROCESSORS = [
    ("crafting_processor_9", (222, 132, 74)),
    ("crafting_processor_81", (214, 62, 62)),
    ("crafting_processor_729", (178, 92, 204)),
]


def base(fill):
    im = Image.new("RGBA", (16, 16), DARK)
    d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 15, 15), outline=EDGE)
    d.rectangle((1, 1, 14, 14), outline=MID)
    d.rectangle((2, 2, 13, 13), fill=fill)
    return im, d


def side(accent):
    im, d = base((36, 42, 47, 255))
    for y in (4, 6, 9, 11):
        d.line((3, y, 12, y), fill=MID)
    d.rectangle((4, 3, 5, 12), fill=LIGHT)
    d.rectangle((10, 3, 11, 12), fill=LIGHT)
    d.rectangle((3, 4, 3, 11), fill=accent)
    d.rectangle((12, 4, 12, 11), fill=accent)
    d.rectangle((6, 7, 9, 8), fill=accent)
    return im


def top(accent, processor=False):
    im, d = base((36, 42, 47, 255))
    d.rectangle((4, 4, 11, 11), fill=(27, 32, 37, 255))
    d.rectangle((5, 5, 10, 10), outline=accent)
    d.rectangle((7, 7, 8, 8), fill=RED if processor else accent)
    if processor:
        d.line((5, 5, 10, 10), fill=accent)
        d.line((10, 5, 5, 10), fill=accent)
    return im


def bottom(accent):
    im, d = base((31, 36, 41, 255))
    d.rectangle((3, 3, 12, 12), outline=MID)
    d.rectangle((5, 5, 10, 10), fill=(23, 27, 31, 255))
    d.point((7, 7), fill=accent)
    d.point((8, 8), fill=accent)
    return im


base_path = "src/main/resources/assets/berlit/textures/blocks"
for name, color in STORAGE:
    side(color).save(f"{base_path}/{name}_side.png")
    top(color).save(f"{base_path}/{name}_top.png")
    bottom(color).save(f"{base_path}/{name}_bottom.png")

for name, color in PROCESSORS:
    side(color).save(f"{base_path}/{name}_side.png")
    top(color, True).save(f"{base_path}/{name}_top.png")
    bottom(color).save(f"{base_path}/{name}_bottom.png")

print("written AE2 crafting storage/processor textures")
