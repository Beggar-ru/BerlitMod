from PIL import Image, ImageDraw

DARK = (24, 28, 32, 255)
EDGE = (12, 14, 16, 255)
MID = (52, 60, 66, 255)
LIGHT = (96, 108, 116, 255)
ACCENT = (79, 214, 205, 255)
GREEN = (84, 204, 126, 255)


def side():
    im = Image.new("RGBA", (16, 16), DARK)
    d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 15, 15), outline=EDGE)
    d.rectangle((1, 1, 14, 14), outline=MID)
    d.rectangle((2, 2, 13, 13), fill=(36, 42, 47, 255))
    for y in (4, 6, 9, 11):
        d.line((3, y, 12, y), fill=MID)
    d.rectangle((4, 3, 5, 12), fill=LIGHT)
    d.rectangle((10, 3, 11, 12), fill=LIGHT)
    d.rectangle((3, 4, 3, 11), fill=ACCENT)
    d.rectangle((12, 4, 12, 11), fill=ACCENT)
    d.rectangle((6, 7, 9, 8), fill=GREEN)
    d.point((7, 6), fill=(190, 255, 220, 255))
    return im


def top():
    im = Image.new("RGBA", (16, 16), DARK)
    d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 15, 15), outline=EDGE)
    d.rectangle((1, 1, 14, 14), outline=MID)
    d.rectangle((2, 2, 13, 13), fill=(36, 42, 47, 255))
    d.rectangle((4, 4, 11, 11), fill=(28, 34, 38, 255))
    d.rectangle((5, 5, 10, 10), outline=ACCENT)
    d.rectangle((7, 7, 8, 8), fill=GREEN)
    d.point((7, 6), fill=(190, 255, 220, 255))
    for x in (3, 12):
        d.line((x, 3, x, 12), fill=LIGHT)
    return im


def bottom():
    im = Image.new("RGBA", (16, 16), DARK)
    d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 15, 15), outline=EDGE)
    d.rectangle((1, 1, 14, 14), fill=(32, 37, 41, 255))
    d.rectangle((3, 3, 12, 12), outline=MID)
    d.rectangle((5, 5, 10, 10), fill=(24, 28, 32, 255))
    d.point((7, 7), fill=ACCENT)
    d.point((8, 8), fill=ACCENT)
    return im


base = "src/main/resources/assets/berlit/textures/blocks"
side().save(f"{base}/molecular_farm_side.png")
top().save(f"{base}/molecular_farm_top.png")
bottom().save(f"{base}/molecular_farm_bottom.png")
print("written molecular_farm textures")
