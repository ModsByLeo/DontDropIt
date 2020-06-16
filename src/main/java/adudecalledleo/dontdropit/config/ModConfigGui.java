package adudecalledleo.dontdropit.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

public class ModConfigGui {
    private static final ModConfig DEFAULTS = new ModConfig();

    public static ConfigBuilder getConfigBuilder() {
        final ModConfig cfg = ModConfigHolder.getConfig();
        ConfigBuilder cb = ConfigBuilder.create().setTitle("dontdropit.config.title");
        ConfigEntryBuilder eb = cb.entryBuilder();
        cb.setSavingRunnable(ModConfigHolder::saveConfig); // TODO validation
        ConfigCategory cDropDelay = cb.getOrCreateCategory("dontdropit.config.category.drop_delay");
        cDropDelay.addEntry(eb.startBooleanToggle("dontdropit.config.enabled", cfg.dropDelay.enabled)
                                    .setDefaultValue(DEFAULTS.dropDelay.enabled).build());
        cDropDelay.addEntry(eb.startIntSlider("dontdropit.config.drop_delay.ticks", cfg.dropDelay.ticks, 5, 100)
                                    .setDefaultValue(DEFAULTS.dropDelay.ticks).build());
        ConfigCategory cDropBlock = cb.getOrCreateCategory("dontdropit.config.category.drop_block");
        cDropBlock.addEntry(eb.startBooleanToggle("dontdropit.config.enabled", cfg.dropBlock.enabled)
                                    .setDefaultValue(DEFAULTS.dropBlock.enabled).build());
        cDropBlock.addEntry(eb.startBooleanToggle("dontdropit.config.drop_block.enchanted", cfg.dropBlock.enchanted)
                                    .setDefaultValue(DEFAULTS.dropBlock.enchanted).build());
        cDropBlock.addEntry(eb.startBooleanToggle("dontdropit.config.drop_block.cursed", cfg.dropBlock.cursed)
                                    .setDefaultValue(DEFAULTS.dropBlock.cursed).build());
        return cb;
    }
}
