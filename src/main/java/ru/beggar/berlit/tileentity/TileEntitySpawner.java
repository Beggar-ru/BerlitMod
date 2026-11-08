package ru.beggar.berlit.tileentity;

public class TileEntitySpawner extends TileEntitySpawnerBase {

    public TileEntitySpawner() {
        super(1);
    }

    @Override
    public int getEntitySlotCount() {
        return 1;
    }

    @Override
    public int getEntitySlotLimit() {
        return 1;
    }

    @Override
    public int getEntitySlotX(int index) {
        return 92;
    }

    @Override
    public int getEntitySlotY(int index) {
        return 22;
    }

    @Override
    public String getTitleKey() {
        return "tile.berlit.spawner.name";
    }
}
