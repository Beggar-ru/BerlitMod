package ru.beggar.berlit;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static String greeting = "Hello World";

    /** Render spawner casings with the alpha holes of their side texture visible. */
    public static boolean transparentSpawners = true;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        greeting = configuration.getString("greeting", Configuration.CATEGORY_GENERAL, greeting, "How shall I greet?");
        transparentSpawners = configuration.getBoolean(
            "transparentSpawners",
            Configuration.CATEGORY_GENERAL,
            transparentSpawners,
            "Render the entity spawners as glass-like blocks so the grid holes on their sides are see-through.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
