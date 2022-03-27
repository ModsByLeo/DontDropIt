package adudecalledleo.dontdropit.config;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import adudecalledleo.dontdropit.ModKeyBindings;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.tag.api.TagRegistry;

public class FavoredChecker {
    private static final HashSet<Item> FAVORED_ITEMS = new HashSet<>();
    private static final HashSet<Enchantment> FAVORED_ENCHANTMENTS = new HashSet<>();
    private static final HashSet<TagKey<Item>> FAVORED_ITEM_TAGS = new HashSet<>();

    static void updateFavoredSets(ModConfig config) {
        updateFavoredSet(FAVORED_ITEMS, config.favorites.items, Registry.ITEM::getOrEmpty);
        updateFavoredSet(FAVORED_ENCHANTMENTS, config.favorites.enchantments, Registry.ENCHANTMENT::getOrEmpty);
        updateFavoredSet(FAVORED_ITEM_TAGS, config.favorites.tags, id -> TagRegistry.stream(Registry.ITEM_KEY)
                .map(TagRegistry.TagEntry::key)
                .filter(key -> id.equals(key.id()))
                .findAny());
    }

    private static <T> void updateFavoredSet(HashSet<T> set, List<String> keys, Function<Identifier, Optional<T>> function) {
        set.clear();
        set.addAll(keys.stream()
                .map(s -> {
                    Identifier id = Identifier.tryParse(s);
                    if (id == null)
                        return null;
                    return function.apply(id).orElse(null);
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
        if (FAVORED_ITEMS.contains(stack.getItem()))
            return true;
        if (FAVORED_ITEM_TAGS.stream().anyMatch(stack::isIn))
            return true;
        Set<Enchantment> enchs = EnchantmentHelper.get(stack).keySet();
        for (Enchantment ench : enchs) {
            if (!FAVORED_ENCHANTMENTS.contains(ench))
                continue;
            if (config.favorites.enchIgnoreInvalidTargets
                    && !(stack.getItem() instanceof EnchantedBookItem) && !ench.isAcceptableItem(stack))
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
