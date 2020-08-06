package adudecalledleo.dontdropit.util;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigUtil {
    public static <T> Consumer<List<T>> makeListSaveConsumer(List<T> destList) {
        return list -> {
            destList.clear();
            destList.addAll(list);
        };
    }

    private static List<String> enchIds;

    @SuppressWarnings("ConstantConditions")
    public static List<String> getAllEnchantmentIds() {
        if (enchIds == null) {
            enchIds = new LinkedList<>();
            for (Identifier enchId : Registry.ENCHANTMENT.getIds())
                if (!Registry.ENCHANTMENT.get(enchId).isCursed())
                    enchIds.add(enchId.toString());
        }
        return new LinkedList<>(enchIds);
    }

    public static <T> Optional<Text> checkIdList(List<String> idList, Registry<T> registry, String errI18n) {
        for (String id : idList) {
            Identifier idObj = new Identifier(id);
            if (!registry.containsId(idObj))
                return Optional.of(new TranslatableText(errI18n, id));
        }
        return Optional.empty();
    }

    public static Optional<Text> checkItemIdList(List<String> idList) {
        return checkIdList(idList, Registry.ITEM, "dontdropit.config.favorites.items.error.bad_id");
    }

    public static Optional<Text> checkEnchantmentIdList(List<String> idList) {
        return checkIdList(idList, Registry.ENCHANTMENT, "dontdropit.config.favorites.enchantments.error.bad_id");
    }

    public static <T1, T2, R> Function<T1, R> composeMappers(Function<T1, T2> map1, Function<T2, R> map2) {
        return key -> map2.apply(map1.apply(key));
    }

    public static <T> List<T> getAllFromRegistry(List<String> idList, Function<Identifier, T> registryGetter) {
        return idList.stream().map(composeMappers(Identifier::new, registryGetter)).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static <T> List<T> getAllFromRegistry(List<String> idList, Registry<T> registry) {
        return getAllFromRegistry(idList, registry::get);
    }
}
