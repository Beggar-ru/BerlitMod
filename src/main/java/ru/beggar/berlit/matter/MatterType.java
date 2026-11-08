package ru.beggar.berlit.matter;

/**
 * Eight kinds of solid matter. Every generator turns one kind into the next one:
 * {@link #STANDARD} is produced from IC2 scrap, then standard becomes violet, violet becomes turquoise and so on
 * up to {@link #RADIANT}.
 */
public enum MatterType {

    STANDARD("standard", 0xE8ECF2, 400, 100),
    VIOLET("violet", 0xA87CE0, 800, 140),
    TURQUOISE("turquoise", 0x4FD6CD, 1600, 180),
    EMERALD("emerald", 0x5BD97A, 3200, 220),
    AZURE("azure", 0x5B8FE0, 6400, 260),
    AMBER("amber", 0xE8B44F, 12800, 300),
    CRIMSON("crimson", 0xE0605B, 25600, 340),
    RADIANT("radiant", 0xFFF2B0, 51200, 380);

    private final String id;
    private final int colour;
    private final int energyPerOperation;
    private final int ticksPerOperation;

    MatterType(String id, int colour, int energyPerOperation, int ticksPerOperation) {
        this.id = id;
        this.colour = colour;
        this.energyPerOperation = energyPerOperation;
        this.ticksPerOperation = ticksPerOperation;
    }

    public String getId() {
        return id;
    }

    public int getColour() {
        return colour;
    }

    public int getEnergyPerOperation() {
        return energyPerOperation;
    }

    public int getTicksPerOperation() {
        return ticksPerOperation;
    }

    /** Internal buffer: four operations' worth, so a single packet cannot stall the machine. */
    public int getEnergyCapacity() {
        return energyPerOperation * 4;
    }

    public boolean isFirst() {
        return ordinal() == 0;
    }

    /** The kind this generator consumes, or null for {@link #STANDARD}. */
    public MatterType getPrevious() {
        return isFirst() ? null : values()[ordinal() - 1];
    }

    public String getItemKey() {
        return "item.berlit.matter_" + id + ".name";
    }

    public String getBlockKey() {
        return "tile.berlit.matter_generator_" + id + ".name";
    }

    public static MatterType byIndex(int index) {
        MatterType[] all = values();
        return all[Math.max(0, Math.min(all.length - 1, index))];
    }
}
