package ru.beggar.berlit;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static String greeting = "Hello World";

    /** Render spawner casings with the alpha holes of their side texture visible. */
    public static boolean transparentSpawners = true;

    /**
     * How many crafts the fast molecular assembler performs per pattern push (1 = one craft, the
     * AE2 default; 64 = a whole stack). 0 or 1 disables batching. This is only used when
     * {@link #fastAssemblerFullTask} is off.
     */
    public static int fastAssemblerBatch = 64;

    /**
     * Let the fast assembler take the entire remaining task for one pattern in a single push.
     * Pre-crafts are handled by {@link #fastAssemblerWholeJob}; when that is off, only the task
     * whose ingredients are already in the ME network is batched.
     */
    public static boolean fastAssemblerFullTask = true;

    /**
     * Run the whole CPU job as one batch: craft every intermediate task from the CPU's reserved
     * ingredients first, then push the final task. This is what turns "logs to planks, then planks
     * to workbenches" into one operation instead of one AE2 round per step. Requires
     * {@link #fastAssemblerFullTask}.
     */
    public static boolean fastAssemblerWholeJob = true;

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
            "Crafts the fast molecular assembler performs per pattern push when full-task mode is off. 1 restores plain AE2 behaviour.");
        fastAssemblerBatch = Math.max(0, Math.min(64, batch));
        fastAssemblerFullTask = configuration.getBoolean(
            "fastAssemblerFullTask",
            Configuration.CATEGORY_GENERAL,
            fastAssemblerFullTask,
            "Craft the entire remaining task in one push when the ME network already has its ingredients.");
        fastAssemblerWholeJob = configuration.getBoolean(
            "fastAssemblerWholeJob",
            Configuration.CATEGORY_GENERAL,
            fastAssemblerWholeJob,
            "Pre-craft every intermediate task of the CPU job in one go, then batch the final task. Requires fastAssemblerFullTask.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
