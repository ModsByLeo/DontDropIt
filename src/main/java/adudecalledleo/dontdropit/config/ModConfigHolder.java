package adudecalledleo.dontdropit.config;

import adudecalledleo.dontdropit.DontDropItMod;
import adudecalledleo.dontdropit.util.ConfigUtil;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.registry.Registry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ModConfigHolder {
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting().create();

    private static ModConfig config;
    private static List<Item> favoriteItems;
    private static List<Enchantment> favoriteEnchantments;
    private static List<Tag<Item>> favoriteTags;

    public static ModConfig getConfig() {
        if (config == null)
            loadConfig();
        return config;
    }

    public static List<Item> getFavoriteItems() {
        return favoriteItems;
    }

    public static List<Enchantment> getFavoriteEnchantments() {
        return favoriteEnchantments;
    }

    public static List<Tag<Item>> getFavoriteTags() {
        return favoriteTags;
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
            } finally {
                updateFavoriteLists();
            }
        } else {
            config = new ModConfig();
            saveConfig();
        }
    }

    public static void saveConfig() {
        updateFavoriteLists();
        try {
            BufferedWriter bw = Files.newBufferedWriter(CONFIG_PATH, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            GSON.toJson(config, bw);
            bw.close();
        } catch (IOException e) {
            DontDropItMod.LOGGER.error("Saving config failed", e);
        }
    }

    private static void updateFavoriteLists() {
        favoriteItems = ConfigUtil.getAllFromRegistry(config.favorites.items, Registry.ITEM);
        favoriteEnchantments = ConfigUtil.getAllFromRegistry(config.favorites.enchantments, Registry.ENCHANTMENT);
        favoriteTags = ConfigUtil.getAllFromRegistry(config.favorites.tags, TagRegistry::item);
    }
}
