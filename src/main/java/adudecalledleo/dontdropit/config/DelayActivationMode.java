package adudecalledleo.dontdropit.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.Collections;
import java.util.Locale;

import static me.shedaniel.autoconfig.util.Utils.getUnsafely;
import static me.shedaniel.autoconfig.util.Utils.setUnsafely;

public enum DelayActivationMode {
    ENABLED, FAVORITES_ONLY, DISABLED;

    public boolean isEnabled(ItemStack stack) {
        switch (this) {
        case DISABLED:
            return false;
        case FAVORITES_ONLY:
            if (!FavoredChecker.isStackFavored(stack))
                return false;
        case ENABLED:
            return true;
        }
        throw new InternalError(this + " is not handled?!");
    }

    public Text toText() {
        MutableText text =
                new TranslatableText("text.autoconfig.dontdropit.general.delayed_drop." + this.name().toLowerCase(Locale.ROOT));
        if (this == DISABLED)
            text = text.styled(style -> style.withColor(Formatting.RED));
        return text;
    }

    private static final DelayActivationMode[] VALUES = values();

    public static <T extends ConfigData> void registerConfigGuiProvider(Class<T> configClass) {
        final ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
        AutoConfig.getGuiRegistry(configClass).registerTypeProvider((i13n, field, config, defaults, registry) ->
                Collections.singletonList(
                        entryBuilder.startSelector(
                                        new TranslatableText(i13n),
                                        VALUES,
                                        getUnsafely(field, config, getUnsafely(field, defaults)))
                                .setDefaultValue(() -> getUnsafely(field, defaults))
                                .setSaveConsumer(newValue -> setUnsafely(field, config, newValue))
                                .setNameProvider(DelayActivationMode::toText)
                                .build()
                ), DropBehaviorOverride.class);
    }
}
