package adudecalledleo.dontdropit.util;

import adudecalledleo.dontdropit.config.ModConfig;
import adudecalledleo.dontdropit.config.ModConfigHolder;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;
import net.minecraft.util.registry.Registry;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FavoritesUtil {
    private static List<Item> favoriteItems;
    private static List<Enchantment> favoriteEnchantments;
    private static List<Tag<Item>> favoriteTags;

    public static List<Item> getFavoriteItems() {
        return favoriteItems;
    }

    public static List<Enchantment> getFavoriteEnchantments() {
        return favoriteEnchantments;
    }

    public static List<Tag<Item>> getFavoriteTags() {
        return favoriteTags;
    }

    private static void updateFavoriteLists(ModConfig config) {
        favoriteItems = ConfigUtil.getAllFromRegistry(config.favorites.items, Registry.ITEM);
        favoriteEnchantments = ConfigUtil.getAllFromRegistry(config.favorites.enchantments, Registry.ENCHANTMENT);
        favoriteTags = ConfigUtil.getAllFromRegistry(config.favorites.tags, TagRegistry::item);
    }

    public static void addConfigListener() {
        ModConfigHolder.addListener(FavoritesUtil::updateFavoriteLists);
    }

    public static boolean isStackFavorite(ItemStack stack) {
        if (!ModConfigHolder.getConfig().favorites.enabled)
            return false;
        if (getFavoriteItems().contains(stack.getItem()))
            return true;
        if (ModConfigHolder.getConfig().favorites.enchIgnoreInvalidTargets) {
            Set<Enchantment> enchantments = EnchantmentHelper.get(stack).keySet();
            for (Enchantment enchantment : enchantments) {
                if (!enchantment.isAcceptableItem(stack))
                    continue;
                if (getFavoriteEnchantments().contains(enchantment))
                    return true;
            }
        } else {
            if (!Collections.disjoint(getFavoriteEnchantments(),
                    EnchantmentHelper.get(stack).keySet()))
                return true;
        }
        List<Tag<Item>> favoriteTags = getFavoriteTags();
        return favoriteTags.stream().anyMatch(tag -> tag.contains(stack.getItem()));
    }
}
