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

public class FavoredChecker {
    private static final HashSet<Item> FAVORED_ITEMS = new HashSet<>();
    private static final HashSet<TagKey<Item>> FAVORED_ITEM_TAGS = new HashSet<>();
    private static final HashSet<Enchantment> FAVORED_ENCHANTMENTS = new HashSet<>();
    private static final HashSet<TagKey<Enchantment>> FAVORED_ENCHANTMENT_TAGS = new HashSet<>();

    static void updateFavoredSets(ModConfig config) {
        updateFavoredSet(FAVORED_ITEMS, config.favorites.items, Registry.ITEM::getOrEmpty);
        updateFavoredSet(FAVORED_ENCHANTMENTS, config.favorites.enchantments, Registry.ENCHANTMENT::getOrEmpty);
        updateFavoredSet(FAVORED_ITEM_TAGS, config.favorites.itemTags, id -> {
            var key = TagKey.of(Registry.ITEM_KEY, id);
            if (Registry.ITEM.containsTag(key)) {
                return Optional.of(key);
            } else {
                return Optional.empty();
            }
        });
        updateFavoredSet(FAVORED_ENCHANTMENT_TAGS, config.favorites.enchantmentTags, id -> {
            var key = TagKey.of(Registry.ENCHANTMENT_KEY, id);
            if (Registry.ENCHANTMENT.containsTag(key)) {
                return Optional.of(key);
            } else {
                return Optional.empty();
            }
        });
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
            boolean matching = FAVORED_ENCHANTMENTS.contains(ench);
            if (!matching) {
                final var enchEntry =
                        Registry.ENCHANTMENT.getEntry(Registry.ENCHANTMENT.getRawId(ench))
                                .orElseThrow(() -> new RuntimeException("Couldn't get registry entry for " + Registry.ENCHANTMENT.getId(ench)));
                matching = FAVORED_ENCHANTMENT_TAGS.stream().anyMatch(enchEntry::isIn);
            }
            if (!matching) {
                continue;
            }

            boolean valid = true;
            if (config.favorites.enchIgnoreInvalidTargets && !(stack.getItem() instanceof EnchantedBookItem)) {
                valid = ench.isAcceptableItem(stack);
            }
            if (valid) {
                return true;
            }
        }
        return false;
    }

    public static boolean canDropStack(ItemStack stack) {
        if (ModKeyBindings.isDown(ModKeyBindings.keyForceDrop))
            return true;
        return !isStackFavored(stack);
    }
}
