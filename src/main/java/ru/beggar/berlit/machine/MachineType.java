package ru.beggar.berlit.machine;

public enum MachineType {

    MACERATOR("macerator", "tile.berlit.machine_macerator.name", 300),
    COMPRESSOR("compressor", "tile.berlit.machine_compressor.name", 400),
    METAL_EXTRUDER("metal_extruder", "tile.berlit.machine_metal_extruder.name", 500),
    METAL_ROLLER("metal_roller", "tile.berlit.machine_metal_roller.name", 500),
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

    /**
     * Each metal machine does exactly one IC2 metal-former operation: the extruder presses
     * shapes, the roller rolls plates, and neither can be switched to the other mode.
     */
    public int getDefaultMode() {
        return this == METAL_ROLLER ? MachineRecipes.MODE_ROLLING : MachineRecipes.MODE_EXTRUDING;
    }

    public String getTextureName() {
        return "berlit:machine_" + id;
    }
}
