package adudecalledleo.dontdropit.config;

import adudecalledleo.dontdropit.ModKeyBindings;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FavoredChecker {
    private static final HashSet<Item> FAVORED_ITEMS = new HashSet<>();
    private static final HashSet<Enchantment> FAVORED_ENCHANTMENTS = new HashSet<>();
    private static final HashSet<Tag<Item>> FAVORED_ITEM_TAGS = new HashSet<>();

    static void updateFavoredSets(ModConfig config) {
        updateFavoredSet(FAVORED_ITEMS, config.favorites.items, Registry.ITEM::get);
        updateFavoredSet(FAVORED_ENCHANTMENTS, config.favorites.enchantments, Registry.ENCHANTMENT::get);
        updateFavoredSet(FAVORED_ITEM_TAGS, config.favorites.tags, TagRegistry::item);
    }

    private static <T> void updateFavoredSet(HashSet<T> set, List<String> keys, Function<Identifier, T> function) {
        set.clear();
        set.addAll(keys.stream()
                .map(s -> {
                    Identifier id = Identifier.tryParse(s);
                    if (id == null)
                        return null;
                    return function.apply(id);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
    }

    public static boolean isStackFavored(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        ModConfig config = ModConfig.get();
        if (!config.favorites.enabled)
            return false;
        Item item = stack.getItem();
        if (FAVORED_ITEMS.contains(item))
            return true;
        if (FAVORED_ITEM_TAGS.stream().anyMatch(tag -> tag.contains(item)))
            return true;
        Set<Enchantment> enchs = EnchantmentHelper.get(stack).keySet();
        for (Enchantment ench : enchs) {
            if (!FAVORED_ENCHANTMENTS.contains(ench))
                continue;
            if (config.favorites.enchIgnoreInvalidTargets && !(item instanceof EnchantedBookItem) && !ench.isAcceptableItem(stack))
                continue;
            return true;
        }
        return false;
    }

    public static boolean canDropStack(ItemStack stack) {
        if (ModKeyBindings.isDown(ModKeyBindings.keyForceDrop))
            return true;
        return !isStackFavored(stack);
    }
}
