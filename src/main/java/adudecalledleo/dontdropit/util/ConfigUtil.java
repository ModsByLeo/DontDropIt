package adudecalledleo.dontdropit.util;

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.StringListBuilder;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;
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

    private static List<Identifier> enchIds;

    @SuppressWarnings("ConstantConditions")
    public static List<Identifier> getAllEnchantmentIds() {
        if (enchIds == null) {
            enchIds = new LinkedList<>();
            for (Identifier enchId : Registry.ENCHANTMENT.getIds())
                if (!Registry.ENCHANTMENT.get(enchId).isCursed())
                    enchIds.add(enchId);
        }
        return new LinkedList<>(enchIds);
    }

    private static Optional<String> safeIdToString(List<String> src, List<Identifier> dst) {
        for (String str : src)
            if (!Identifier.isValid(str))
                return Optional.of(str);
        dst.clear();
        src.stream().map(Identifier::tryParse).filter(Objects::nonNull).forEach(dst::add);
        return Optional.empty();
    }

    public static StringListBuilder startIdList(ConfigEntryBuilder eb, Text name, List<Identifier> list, List<Identifier> defaultList,
            Function<List<Identifier>, Optional<Text>> errorSupplier) {
        List<String> dList = list.stream().map(String::valueOf).collect(Collectors.toList());
        List<String> defaultDList = defaultList.stream().map(String::valueOf).collect(Collectors.toList());
        StringListBuilder slb = eb.startStrList(name, dList)
                .setSaveConsumer(strList -> safeIdToString(strList, list))
                .setDefaultValue(defaultDList);
        slb.setErrorSupplier(strList -> {
            List<Identifier> values = new ArrayList<>();
            Optional<String> result = safeIdToString(strList, values);
            if (result.isPresent())
                return Optional.of(new TranslatableText("dontdropit.config.bad_id", result.get()));
            if (errorSupplier != null)
                return errorSupplier.apply(values);
            return Optional.empty();
        });
        return slb;
    }

    public static StringListBuilder startIdList(ConfigEntryBuilder eb, Text name, List<Identifier> list, List<Identifier> defaultList) {
        return startIdList(eb, name, list, defaultList, null);
    }

    public static <T> Optional<Text> checkIdList(List<Identifier> idList, Registry<T> registry, String errI18n) {
        for (Identifier id : idList) {
            if (id == null)
                continue;
            if (!registry.containsId(id))
                return Optional.of(new TranslatableText(errI18n, id.toString()));
        }
        return Optional.empty();
    }

    public static Optional<Text> checkItemIdList(List<Identifier> idList) {
        return checkIdList(idList, Registry.ITEM, "dontdropit.config.favorites.items.error.bad_id");
    }

    public static Optional<Text> checkEnchantmentIdList(List<Identifier> idList) {
        return checkIdList(idList, Registry.ENCHANTMENT, "dontdropit.config.favorites.enchantments.error.bad_id");
    }

    public static <T> List<T> getAllFromRegistry(List<Identifier> idList, Function<Identifier, T> registryGetter) {
        return idList.stream().map(registryGetter).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static <T> List<T> getAllFromRegistry(List<Identifier> idList, Registry<T> registry) {
        return getAllFromRegistry(idList, registry::get);
    }
}
