package ru.beggar.berlit.machine;

public enum MachineType {

    MACERATOR("macerator", "tile.berlit.machine_macerator.name", 300),
    COMPRESSOR("compressor", "tile.berlit.machine_compressor.name", 400),
    METAL_FORMER("metal_former", "tile.berlit.machine_metal_former.name", 500),
    ELECTRIC_FURNACE("electric_furnace", "tile.berlit.machine_electric_furnace.name", 200);

    private final String id;
    private final String titleKey;
    private final int energyPerItem;

    MachineType(String id, String titleKey, int energyPerItem) {
        this.id = id;
        this.titleKey = titleKey;
        this.energyPerItem = energyPerItem;
    }

    public String getId() {
        return id;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public int getEnergyPerItem() {
        return energyPerItem;
    }

    public String getTextureName() {
        return "berlit:machine_" + id;
    }
}
