package adudecalledleo.dontdropit.util;

import adudecalledleo.dontdropit.config.ModConfigHolder;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ConfigUtil {
    public static <T> Consumer<List<T>> makeListSaveConsumer(List<T> destList) {
        return list -> {
            destList.clear();
            destList.addAll(list);
        };
    }

    private static List<String> enchIds;

    public static List<String> getAllEnchantmentIds() {
        if (enchIds == null) {
            enchIds = new LinkedList<>();
            for (Identifier enchId : Registry.ENCHANTMENT.getIds())
                if (!Registry.ENCHANTMENT.get(enchId).isCursed())
                    enchIds.add(enchId.toString());
        }
        return new LinkedList<>(enchIds);
    }

    public static <T> Optional<String> checkIdList(List<String> idList, Registry<T> registry, String errI18n) {
        for (String id : idList) {
            Identifier idObj = new Identifier(id);
            if (!registry.containsId(idObj))
                return Optional.of(I18n.translate(errI18n, id));
        }
        return Optional.empty();
    }

    public static Optional<String> checkItemIdList(List<String> idList) {
        return checkIdList(idList, Registry.ITEM, "dontdropit.config.favorites.items.error.bad_id");
    }

    public static Optional<String> checkEnchantmentIdList(List<String> idList) {
        return checkIdList(idList, Registry.ENCHANTMENT, "dontdropit.config.favorites.enchantments.error.bad_id");
    }

    public static <T> List<T> getAllFromRegistry(List<String> idList, Registry<T> registry) {
        return idList.stream().map(id -> registry.get(new Identifier(id))).collect(Collectors.toList());
    }

    public static boolean isStackFavorite(ItemStack stack) {
        if (!ModConfigHolder.getConfig().favorites.enabled)
            return false;
        if (ModConfigHolder.getFavoriteItems().contains(stack.getItem()))
            return true;
        return !Collections.disjoint(ModConfigHolder.getFavoriteEnchantments(),
                EnchantmentHelper.getEnchantments(stack).keySet());
    }
}
