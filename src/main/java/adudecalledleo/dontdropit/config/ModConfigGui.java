package adudecalledleo.dontdropit.config;

import adudecalledleo.dontdropit.util.ConfigUtil;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class ModConfigGui {
    public static ConfigBuilder getConfigBuilder() {
        final ModConfig cfg = ModConfigHolder.getConfig();
        ConfigBuilder cb = ConfigBuilder.create().setTitle(k("title"));
        cb.setSavingRunnable(ModConfigHolder::saveConfig);
        addGeneralCategory(cfg, cb);
        addDropDelayCategory(cfg, cb);
        addFavoritesCategory(cfg, cb);
        return cb;
    }

    private static final ModConfig DEFAULTS = new ModConfig();

    @SuppressWarnings("rawtypes")
    private static final Function<Enum, String> OOB_DROP_CLICK_OVERRIDE_NAME_PROVIDER = anEnum ->
            (anEnum == ModConfig.General.OOBClickDropOverride.DISABLED ? Formatting.RED.toString() : "") +
                    I18n.translate(k("general.oob_click_drop_behavior." + anEnum.name().toLowerCase()));

    private static String k(final String key) {
        return "dontdropit.config." + key;
    }

    private static Supplier<Optional<String[]>> tooltip(final String key) {
        final Optional<String[]> data = Optional.of(new String[] { I18n.translate(key) });
        return () -> data;
    }

    private static Supplier<Optional<String[]>> tooltip(final String... keys) {
        final Optional<String[]> data = Optional.of(Arrays.stream(keys).map(key -> I18n.translate(key)).toArray(String[]::new));
        return () -> data;
    }

    private static Supplier<Optional<String[]>> tooltip(final String key, final int count) {
        if (count == 1)
            return tooltip(key);
        return tooltip(IntStream.range(0, count).mapToObj(i -> String.format("%s[%d]", key, i)).toArray(String[]::new));
    }

    private static void addGeneralCategory(ModConfig cfg, ConfigBuilder cb) {
        ConfigEntryBuilder eb = cb.entryBuilder();
        ConfigCategory cGeneral = cb.getOrCreateCategory(k("category.general"));
        cGeneral.addEntry(eb.startEnumSelector(k("general.oob_click_drop_behavior"),
                ModConfig.General.OOBClickDropOverride.class, cfg.general.oobDropClickOverride)
                .setSaveConsumer(value -> cfg.general.oobDropClickOverride = value)
                .setEnumNameProvider(OOB_DROP_CLICK_OVERRIDE_NAME_PROVIDER)
                .setTooltipSupplier(tooltip(k("general.oob_click_drop_behavior.tooltip"), 2))
                .setDefaultValue(DEFAULTS.general.oobDropClickOverride).build());
    }

    private static void addDropDelayCategory(ModConfig cfg, ConfigBuilder cb) {
        ConfigEntryBuilder eb = cb.entryBuilder();
        ConfigCategory cDropDelay = cb.getOrCreateCategory(k("category.drop_delay"));
        cDropDelay.addEntry(eb.startBooleanToggle(k("enabled"), cfg.dropDelay.enabled)
                .setSaveConsumer(value -> cfg.dropDelay.enabled = value)
                .setTooltipSupplier(tooltip(k("drop_delay.enabled.tooltip"), 3))
                .setDefaultValue(DEFAULTS.dropDelay.enabled).build());
        cDropDelay.addEntry(eb.startIntSlider(k("drop_delay.ticks"), cfg.dropDelay.ticks, 5, 100)
                .setSaveConsumer(value -> cfg.dropDelay.ticks = value)
                .setTooltipSupplier(tooltip(k("drop_delay.ticks.tooltip"), 2))
                .setDefaultValue(DEFAULTS.dropDelay.ticks).build());
    }

    private static void addFavoritesCategory(ModConfig cfg, ConfigBuilder cb) {
        ConfigEntryBuilder eb = cb.entryBuilder();
        ConfigCategory cFavorites = cb.getOrCreateCategory(k("category.favorites"));
        cFavorites.addEntry(eb.startBooleanToggle(k("enabled"), cfg.favorites.enabled)
                .setSaveConsumer(value -> cfg.favorites.enabled = value)
                .setTooltipSupplier(tooltip(k("favorites.enabled.tooltip"), 2))
                .setDefaultValue(DEFAULTS.favorites.enabled).build());
        cFavorites.addEntry(eb.startStrList(k("favorites.items"), cfg.favorites.items)
                .setSaveConsumer(ConfigUtil.makeListSaveConsumer(cfg.favorites.items))
                .setErrorSupplier(ConfigUtil::checkItemIdList)
                .setTooltipSupplier(tooltip(k("favorites.items.tooltip"), k("favorites.tooltip")))
                .setDefaultValue(DEFAULTS.favorites.items).build());
        cFavorites.addEntry(eb.startStrList(k("favorites.enchantments"), cfg.favorites.enchantments)
                .setSaveConsumer(ConfigUtil.makeListSaveConsumer(cfg.favorites.enchantments))
                .setErrorSupplier(ConfigUtil::checkEnchantmentIdList)
                .setTooltipSupplier(tooltip(k("favorites.enchantments.tooltip"), k("favorites.tooltip")))
                .setDefaultValue(DEFAULTS.favorites.enchantments).build());
        cFavorites.addEntry(eb.startStrList(k("favorites.tags"), cfg.favorites.tags)
                .setSaveConsumer(ConfigUtil.makeListSaveConsumer(cfg.favorites.tags))
                .setTooltipSupplier(tooltip(k("favorites.tags.tooltip"), k("favorites.tooltip")))
                .setDefaultValue(DEFAULTS.favorites.tags).build());
    }
}
