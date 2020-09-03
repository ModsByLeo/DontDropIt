package adudecalledleo.dontdropit.config;

import adudecalledleo.dontdropit.util.ConfigUtil;
import adudecalledleo.lionutils.config.AbstractConfig;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;

public class ModConfig extends AbstractConfig {
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
            enchantments = ConfigUtil.getAllEnchantmentIds();
            tags = new LinkedList<>();
        }

        public boolean enabled = true;
        public boolean drawOverlay = true;
        public List<Identifier> items;
        public List<Identifier> enchantments;
        public boolean enchIgnoreInvalidTargets = true;
        public List<Identifier> tags;
    }

    public Favorites favorites;
}
