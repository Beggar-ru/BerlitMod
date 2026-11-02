package ru.beggar.berlit.tileentity;

public class TileEntitySpawnerT2 extends TileEntitySpawnerBase {

    public TileEntitySpawnerT2() {
        super(10);
    }

    @Override
    public int getEntitySlotCount() {
        return 10;
    }

    @Override
    public int getEntitySlotLimit() {
        return 10;
    }

    @Override
    public int getEntitySlotX(int index) {
        return 8 + (index % 5) * 18;
    }

    @Override
    public int getEntitySlotY(int index) {
        return 17 + (index / 5) * 18;
    }

    @Override
    public int getUpgradeSlotX(int index) {
        return 98 + (index % 4) * 18;
    }

    @Override
    public int getUpgradeSlotY(int index) {
        return 17 + (index / 4) * 18;
    }

    @Override
    public String getGuiTexture() {
        return "berlit:textures/gui/spawner_t2.png";
    }

    @Override
    public String getTitleKey() {
        return "tile.berlit.spawner_t2.name";
    }
}
