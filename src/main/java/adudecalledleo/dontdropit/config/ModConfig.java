package adudecalledleo.dontdropit.config;

import adudecalledleo.dontdropit.util.ConfigUtil;

import java.util.LinkedList;
import java.util.List;

public class ModConfig {
    public ModConfig() {
        dropDelay = new DropDelay();
        favorites = new Favorites();
    }

    public static class DropDelay {
        public boolean enabled = true;
        public int ticks = 10;
    }

    public DropDelay dropDelay;

    public static class Favorites {
        public Favorites() {
            items = new LinkedList<>();
            enchantments = new LinkedList<>(ConfigUtil.getAllEnchantmentIds());
        }

        public boolean enabled = true;
        public List<String> items;
        public List<String> enchantments;
    }

    public Favorites favorites;
}
