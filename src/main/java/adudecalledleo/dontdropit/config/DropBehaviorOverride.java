package adudecalledleo.dontdropit.config;

import me.shedaniel.clothconfig2.gui.entries.SelectionListEntry;

import java.util.Locale;

public enum DropBehaviorOverride implements SelectionListEntry.Translatable {
    FAVORITE_ITEMS, ALL_ITEMS, DISABLED;

    @Override
    public String getKey() {
        return "text.autoconfig.dontdropit.general.drop_behavior." + this.name().toLowerCase(Locale.ROOT);
    }
}
