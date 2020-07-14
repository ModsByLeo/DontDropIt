package adudecalledleo.dontdropit.config;

import adudecalledleo.dontdropit.util.ConfigUtil;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class ModConfigGui {
    public static ConfigBuilder getConfigBuilder() {
        final ModConfig cfg = ModConfigHolder.getConfig();
        ConfigBuilder cb = ConfigBuilder.create().setTitle(t("title"));
        cb.setSavingRunnable(ModConfigHolder::saveConfig);
        addGeneralCategory(cfg, cb);
        addDropDelayCategory(cfg, cb);
        addFavoritesCategory(cfg, cb);
        return cb;
    }

    private static final ModConfig DEFAULTS = new ModConfig();

    @SuppressWarnings("rawtypes")
    private static final Function<Enum, Text> OOB_DROP_CLICK_OVERRIDE_NAME_PROVIDER = value -> {
        MutableText text = new TranslatableText(k("general.oob_click_drop_behavior." + value.toString()
                                                                                            .toLowerCase()));
        if (value == ModConfig.General.OOBClickDropOverride.DISABLED)
            text = text.formatted(Formatting.RED);
        return text;
    };

    private static String k(String key) {
        return "dontdropit.config." + key;
    }

    private static Text t(String key) {
        return new TranslatableText(k(key));
    }

    private static Supplier<Optional<Text[]>> tooltip(final String key) {
        final Optional<Text[]> data = Optional.of(new Text[] { new TranslatableText(key) });
        return () -> data;
    }

    private static Supplier<Optional<Text[]>> tooltip(final String... keys) {
        final Optional<Text[]> data = Optional.of(Arrays.stream(keys).map(TranslatableText::new).toArray(Text[]::new));
        return () -> data;
    }

    private static Supplier<Optional<Text[]>> tooltip(final String key, final int count) {
        if (count == 1)
            return tooltip(key);
        return tooltip(IntStream.range(0, count).mapToObj(i -> String.format("%s[%d]", key, i)).toArray(String[]::new));
    }

    private static void addGeneralCategory(ModConfig cfg, ConfigBuilder cb) {
        ConfigEntryBuilder eb = cb.entryBuilder();
        ConfigCategory cGeneral = cb.getOrCreateCategory(t("category.general"));
        cGeneral.addEntry(eb.startEnumSelector(t("general.oob_click_drop_behavior"),
                ModConfig.General.OOBClickDropOverride.class, cfg.general.oobDropClickOverride)
                .setSaveConsumer(value -> cfg.general.oobDropClickOverride = value)
                .setEnumNameProvider(OOB_DROP_CLICK_OVERRIDE_NAME_PROVIDER)
                .setTooltipSupplier(tooltip(k("general.oob_click_drop_behavior.tooltip"), 2))
                .setDefaultValue(DEFAULTS.general.oobDropClickOverride).build());
    }

    private static void addDropDelayCategory(ModConfig cfg, ConfigBuilder cb) {
        ConfigEntryBuilder eb = cb.entryBuilder();
        ConfigCategory cDropDelay = cb.getOrCreateCategory(t("category.drop_delay"));
        cDropDelay.addEntry(eb.startBooleanToggle(t("enabled"), cfg.dropDelay.enabled)
                .setSaveConsumer(value -> cfg.dropDelay.enabled = value)
                .setTooltipSupplier(tooltip(k("drop_delay.enabled.tooltip"), 3))
                .setDefaultValue(DEFAULTS.dropDelay.enabled).build());
        cDropDelay.addEntry(eb.startIntSlider(t("drop_delay.ticks"), cfg.dropDelay.ticks, 5, 100)
                .setSaveConsumer(value -> cfg.dropDelay.ticks = value)
                .setTooltipSupplier(tooltip(k("drop_delay.ticks.tooltip"), 2))
                .setDefaultValue(DEFAULTS.dropDelay.ticks).build());
        cDropDelay.addEntry(eb.startBooleanToggle(t("drop_delay.do_delay_once"), cfg.dropDelay.doDelayOnce)
                .setSaveConsumer(value -> cfg.dropDelay.doDelayOnce = value)
                .setTooltipSupplier(tooltip(k("drop_delay.do_delay_once.tooltip"), 2))
                .setDefaultValue(DEFAULTS.dropDelay.doDelayOnce).build());
    }

    private static void addFavoritesCategory(ModConfig cfg, ConfigBuilder cb) {
        ConfigEntryBuilder eb = cb.entryBuilder();
        ConfigCategory cFavorites = cb.getOrCreateCategory(t("category.favorites"));
        cFavorites.addEntry(eb.startBooleanToggle(t("enabled"), cfg.favorites.enabled)
                .setSaveConsumer(value -> cfg.favorites.enabled = value)
                .setTooltipSupplier(tooltip(k("favorites.enabled.tooltip"), 2))
                .setDefaultValue(DEFAULTS.favorites.enabled).build());
        cFavorites.addEntry(eb.startStrList(t("favorites.items"), cfg.favorites.items)
                .setSaveConsumer(ConfigUtil.makeListSaveConsumer(cfg.favorites.items))
                .setErrorSupplier(ConfigUtil::checkItemIdList)
                .setTooltipSupplier(tooltip(k("favorites.items.tooltip"), k("favorites.tooltip")))
                .setDefaultValue(DEFAULTS.favorites.items).build());
        cFavorites.addEntry(eb.startStrList(t("favorites.enchantments"), cfg.favorites.enchantments)
                .setSaveConsumer(ConfigUtil.makeListSaveConsumer(cfg.favorites.enchantments))
                .setErrorSupplier(ConfigUtil::checkEnchantmentIdList)
                .setTooltipSupplier(tooltip(k("favorites.enchantments.tooltip"), k("favorites.tooltip")))
                .setDefaultValue(DEFAULTS.favorites.enchantments).build());
        cFavorites.addEntry(eb.startBooleanToggle(t("favorites.enchantments.ignore_invalid_targets"), cfg.favorites.enchIgnoreInvalidTargets)
                .setSaveConsumer(value -> cfg.favorites.enchIgnoreInvalidTargets = value)
                .setTooltipSupplier(tooltip(k("favorites.enchantments.ignore_invalid_targets.tooltip"), 2))
                .setDefaultValue(DEFAULTS.favorites.enchIgnoreInvalidTargets).build());
        cFavorites.addEntry(eb.startStrList(t("favorites.tags"), cfg.favorites.tags)
                .setSaveConsumer(ConfigUtil.makeListSaveConsumer(cfg.favorites.tags))
                .setTooltipSupplier(tooltip(k("favorites.tags.tooltip"), k("favorites.tooltip")))
                .setDefaultValue(DEFAULTS.favorites.tags).build());
    }
}
