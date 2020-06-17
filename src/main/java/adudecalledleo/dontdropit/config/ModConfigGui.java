package adudecalledleo.dontdropit.config;

import adudecalledleo.dontdropit.util.ConfigUtil;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import java.util.function.Function;

public class ModConfigGui {
    private static final ModConfig DEFAULTS = new ModConfig();

    @SuppressWarnings("rawtypes")
    private static final Function<Enum, String> OOB_DROP_CLICK_OVERRIDE_NAME_PROVIDER = anEnum ->
        "dontdropit.config.general.oob_click_drop_behavior." + anEnum.name().toLowerCase();

    public static ConfigBuilder getConfigBuilder() {
        final ModConfig cfg = ModConfigHolder.getConfig();
        ConfigBuilder cb = ConfigBuilder.create().setTitle("dontdropit.config.title");
        ConfigEntryBuilder eb = cb.entryBuilder();
        cb.setSavingRunnable(ModConfigHolder::saveConfig);
        ConfigCategory cGeneral = cb.getOrCreateCategory("dontdropit.config.category.general");
        cGeneral.addEntry(eb.startEnumSelector("dontdropit.config.general.oob_click_drop_behavior",
                ModConfig.General.OOBClickDropOverride.class, cfg.general.oobDropClickOverride)
                                    .setSaveConsumer(value -> cfg.general.oobDropClickOverride = value)
                                    .setEnumNameProvider(OOB_DROP_CLICK_OVERRIDE_NAME_PROVIDER)
                                    .setDefaultValue(DEFAULTS.general.oobDropClickOverride).build());
        ConfigCategory cDropDelay = cb.getOrCreateCategory("dontdropit.config.category.drop_delay");
        cDropDelay.addEntry(eb.startBooleanToggle("dontdropit.config.drop_delay.enabled", cfg.dropDelay.enabled)
                                    .setSaveConsumer(value -> cfg.dropDelay.enabled = value)
                                    .setDefaultValue(DEFAULTS.dropDelay.enabled).build());
        cDropDelay.addEntry(eb.startIntSlider("dontdropit.config.drop_delay.ticks", cfg.dropDelay.ticks, 5, 100)
                                    .setSaveConsumer(value -> cfg.dropDelay.ticks = value)
                                    .setDefaultValue(DEFAULTS.dropDelay.ticks).build());
        ConfigCategory cFavorites = cb.getOrCreateCategory("dontdropit.config.category.favorites");
        cFavorites.addEntry(eb.startBooleanToggle("dontdropit.config.favorites.enabled", cfg.favorites.enabled)
                                    .setSaveConsumer(value -> cfg.favorites.enabled = value)
                                    .setDefaultValue(DEFAULTS.favorites.enabled).build());
        cFavorites.addEntry(eb.startStrList("dontdropit.config.favorites.items", cfg.favorites.items)
                                    .setSaveConsumer(ConfigUtil.makeListSaveConsumer(cfg.favorites.items))
                                    .setErrorSupplier(ConfigUtil::checkItemIdList)
                                    .setDefaultValue(DEFAULTS.favorites.items).build());
        cFavorites.addEntry(eb.startStrList("dontdropit.config.favorites.enchantments", cfg.favorites.enchantments)
                                    .setSaveConsumer(ConfigUtil.makeListSaveConsumer(cfg.favorites.enchantments))
                                    .setErrorSupplier(ConfigUtil::checkEnchantmentIdList)
                                    .setDefaultValue(DEFAULTS.favorites.enchantments).build());
        cFavorites.addEntry(eb.startStrList("dontdropit.config.favorites.tags", cfg.favorites.tags)
                                    .setSaveConsumer(ConfigUtil.makeListSaveConsumer(cfg.favorites.tags))
                                    .setDefaultValue(DEFAULTS.favorites.tags).build());
        return cb;
    }
}
