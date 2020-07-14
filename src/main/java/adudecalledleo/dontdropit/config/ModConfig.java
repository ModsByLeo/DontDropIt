package adudecalledleo.dontdropit.config;

import adudecalledleo.dontdropit.util.ConfigUtil;

import java.util.LinkedList;
import java.util.List;

public class ModConfig {
    public ModConfig() {
        general = new General();
        dropDelay = new DropDelay();
        favorites = new Favorites();
    }

    public static class General {
        public enum OOBClickDropOverride {
            FAVORITE_ITEMS, ALL_ITEMS, DISABLED
        }

        public OOBClickDropOverride oobDropClickOverride = OOBClickDropOverride.FAVORITE_ITEMS;
    }

    public General general;

    public static class DropDelay {
        public boolean enabled = true;
        public int ticks = 10;
        public boolean doDelayOnce = false;
    }

    public DropDelay dropDelay;

    public static class Favorites {
        public Favorites() {
            items = new LinkedList<>();
            enchantments = new LinkedList<>(ConfigUtil.getAllEnchantmentIds());
            tags = new LinkedList<>();
        }

        public boolean enabled = true;
        public List<String> items;
        public List<String> enchantments;
        public boolean enchIgnoreInvalidTargets = true;
        public List<String> tags;
    }

    public Favorites favorites;
}
