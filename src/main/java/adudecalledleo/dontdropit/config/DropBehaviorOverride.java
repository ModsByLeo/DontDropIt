package adudecalledleo.dontdropit.config;

import java.util.Collections;
import java.util.Locale;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static me.shedaniel.autoconfig.util.Utils.getUnsafely;
import static me.shedaniel.autoconfig.util.Utils.setUnsafely;

public enum DropBehaviorOverride {
    FAVORITE_ITEMS, ALL_ITEMS, DISABLED;

    public Text toText() {
        var text =
                Text.translatable("text.autoconfig.dontdropit.general.drop_behavior."
                        + this.name().toLowerCase(Locale.ROOT));
        if (this == DISABLED) {
            text = text.styled(style -> style.withColor(Formatting.RED));
        }
        return text;
    }

    private static final DropBehaviorOverride[] VALUES = values();

    public static <T extends ConfigData> void registerConfigGuiProvider(Class<T> configClass) {
        final ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
        AutoConfig.getGuiRegistry(configClass).registerTypeProvider((i13n, field, config, defaults, registry) ->
                Collections.singletonList(
                    entryBuilder.startSelector(
                            Text.translatable(i13n),
                                    VALUES,
                                    getUnsafely(field, config, getUnsafely(field, defaults)))
                            .setDefaultValue(() -> getUnsafely(field, defaults))
                            .setSaveConsumer(newValue -> setUnsafely(field, config, newValue))
                            .setNameProvider(DropBehaviorOverride::toText)
                            .build()
                ), DropBehaviorOverride.class);
    }
}
