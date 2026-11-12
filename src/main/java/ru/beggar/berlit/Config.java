package ru.beggar.berlit;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static String greeting = "Hello World";

    /** Render spawner casings with the alpha holes of their side texture visible. */
    public static boolean transparentSpawners = true;

    /**
     * How many crafts the fast molecular assembler performs per pattern push (1 = one craft, the
     * AE2 default; 64 = a whole stack). 0 or 1 disables batching.
     */
    public static int fastAssemblerBatch = 64;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        greeting = configuration.getString("greeting", Configuration.CATEGORY_GENERAL, greeting, "How shall I greet?");
        transparentSpawners = configuration.getBoolean(
            "transparentSpawners",
            Configuration.CATEGORY_GENERAL,
            transparentSpawners,
            "Render the entity spawners as glass-like blocks so the grid holes on their sides are see-through.");
        int batch = configuration.getInt(
            "fastAssemblerBatch",
            Configuration.CATEGORY_GENERAL,
            fastAssemblerBatch,
            0,
            64,
            "Crafts the fast molecular assembler performs per pattern push. 1 restores plain AE2 behaviour.");
        fastAssemblerBatch = Math.max(0, Math.min(64, batch));

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
