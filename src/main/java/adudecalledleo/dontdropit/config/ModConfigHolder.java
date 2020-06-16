package adudecalledleo.dontdropit.config;

import adudecalledleo.dontdropit.DontDropItMod;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ModConfigHolder {
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting().create();

    private static ModConfig config;

    public static ModConfig getConfig() {
        if (config == null)
            loadConfig();
        return config;
    }

    private static final Path CONFIG_PATH = Paths.get(FabricLoader.getInstance().getConfigDirectory().toURI())
                                                    .resolve("dontdropit.json");

    public static void loadConfig() {
        if (CONFIG_PATH.toFile().exists()) {
            try {
                BufferedReader br = Files.newBufferedReader(CONFIG_PATH);
                config = GSON.fromJson(br, ModConfig.class);
                br.close();
            } catch (IOException e) {
                DontDropItMod.LOGGER.error("Loading config failed, continuing with default values", e);
                config = new ModConfig();
            }
        } else {
            config = new ModConfig();
            saveConfig();
        }
    }

    public static void saveConfig() {
        try {
            BufferedWriter bw = Files.newBufferedWriter(CONFIG_PATH, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            GSON.toJson(config, bw);
            bw.close();
        } catch (IOException e) {
            DontDropItMod.LOGGER.error("Saving config failed", e);
        }
    }
}
