from PIL import Image, ImageDraw

DARK = (22, 25, 29, 255)
EDGE = (10, 11, 13, 255)
MID = (52, 60, 67, 255)
LIGHT = (105, 118, 128, 255)
CYAN = (79, 214, 205, 255)
RED = (214, 62, 62, 255)


def base(fill):
    im = Image.new("RGBA", (16, 16), DARK)
    d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 15, 15), outline=EDGE)
    d.rectangle((1, 1, 14, 14), outline=MID)
    d.rectangle((2, 2, 13, 13), fill=fill)
    return im, d


im, d = base((36, 42, 47, 255))
for y in (4, 6, 9, 11):
    d.line((3, y, 12, y), fill=MID)
d.rectangle((4, 3, 5, 12), fill=LIGHT)
d.rectangle((10, 3, 11, 12), fill=LIGHT)
d.rectangle((3, 4, 3, 11), fill=CYAN)
d.rectangle((12, 4, 12, 11), fill=CYAN)
d.rectangle((6, 7, 9, 8), fill=RED)
d.point((7, 6), fill=(255, 210, 210, 255))
im.save("src/main/resources/assets/berlit/textures/blocks/digital_spawner_side.png")

im, d = base((36, 42, 47, 255))
d.rectangle((4, 4, 11, 11), fill=(27, 32, 37, 255))
d.rectangle((5, 5, 10, 10), outline=CYAN)
d.rectangle((7, 7, 8, 8), fill=RED)
for x in (3, 12):
    d.line((x, 3, x, 12), fill=LIGHT)
im.save("src/main/resources/assets/berlit/textures/blocks/digital_spawner_top.png")

im, d = base((31, 36, 41, 255))
d.rectangle((3, 3, 12, 12), outline=MID)
d.rectangle((5, 5, 10, 10), fill=(23, 27, 31, 255))
d.point((7, 7), fill=RED)
d.point((8, 8), fill=CYAN)
im.save("src/main/resources/assets/berlit/textures/blocks/digital_spawner_bottom.png")
print("written digital spawner textures")
