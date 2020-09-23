package adudecalledleo.dontdropit.config;

import adudecalledleo.dontdropit.DontDropIt;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.shedaniel.clothconfig2.gui.entries.SelectionListEntry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

import java.util.*;

import static adudecalledleo.dontdropit.config.ModConfigLogger.LOGGER;

@Config(name = DontDropIt.MOD_ID)
public class ModConfig implements ConfigData {
    public static class General {
        public enum DropBehaviorOverride implements SelectionListEntry.Translatable {
            FAVORITE_ITEMS, ALL_ITEMS, DISABLED;

            @Override
            public String getKey() {
                return "text.autoconfig.dontdropit.general.drop_behavior." + this.name().toLowerCase(Locale.ROOT);
            }
        }

        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        @ConfigEntry.Gui.Tooltip(count = 2)
        public DropBehaviorOverride oobDropClickOverride = DropBehaviorOverride.FAVORITE_ITEMS;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        @ConfigEntry.Gui.Tooltip(count = 2)
        public DropBehaviorOverride cursorCloseDropOverride = DropBehaviorOverride.ALL_ITEMS;
    }

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.TransitiveObject
    public General general = new General();

    public static class DropDelay {
        @ConfigEntry.Gui.Tooltip(count = 3)
        public boolean enabled = true;
        @ConfigEntry.BoundedDiscrete(max = 200, min = 1)
        @ConfigEntry.Gui.Tooltip(count = 3)
        public long ticks = 10;
        @ConfigEntry.Gui.Tooltip(count = 3)
        public boolean doDelayOnce = false;
    }

    @ConfigEntry.Category("drop_delay")
    @ConfigEntry.Gui.TransitiveObject
    public DropDelay dropDelay = new DropDelay();

    public static class Favorites {
        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean enabled = true;
        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean disableShiftClick = false;
        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean drawOverlay = true;
        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip
        public List<String> items = getRareItemIds();
        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip
        public List<String> enchantments = getEnchantmentIds();
        @ConfigEntry.Gui.Tooltip(count = 3)
        public boolean enchIgnoreInvalidTargets = true;
        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip(count = 2)
        public List<String> tags = new ArrayList<>();

        private static List<String> getRareItemIds() {
            ArrayList<String> itemIds = new ArrayList<>();
            for (Identifier id : Registry.ITEM.getIds()) {
                Item item = Registry.ITEM.get(id);
                if (item.getRarity(new ItemStack(item)) != Rarity.COMMON)
                    itemIds.add(id.toString());
            }
            return itemIds;
        }

        private static List<String> getEnchantmentIds() {
            ArrayList<String> enchIds = new ArrayList<>();
            for (Identifier id : Registry.ENCHANTMENT.getIds()) {
                Enchantment enchantment = Registry.ENCHANTMENT.get(id);
                if (enchantment == null || enchantment.isCursed())
                    continue;
                enchIds.add(id.toString());
            }
            return enchIds;
        }

        void postLoad() {
            items = init(items);
            enchantments = init(enchantments);
            tags = init(tags);
            removeInvalidIdsFrom(items, "items", Registry.ITEM);
            removeInvalidIdsFrom(enchantments, "enchantments", Registry.ENCHANTMENT);
        }

        private List<String> init(List<String> list) {
            if (list == null)
                return new ArrayList<>();
            else
                return new ArrayList<>(new LinkedHashSet<>(list)); // deduplicate
        }

        private void removeInvalidIdsFrom(List<String> list, String listName, Registry<?> registry) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String idStr = it.next();
                Identifier id = Identifier.tryParse(idStr);
                if (id == null) {
                    LOGGER.warn("Favorites: Found invalid identifier \"{}\" in favored {} list, removing", idStr, listName);
                    it.remove();
                    continue;
                }
                if (!registry.containsId(id)) {
                    LOGGER.warn("Favorites: Found unregistered identifier \"{}\" in favored {} list, removing", id.toString(), listName);
                    it.remove();
                }
            }
        }
    }

    @ConfigEntry.Category("favorites")
    @ConfigEntry.Gui.TransitiveObject
    public Favorites favorites = new Favorites();

    @Override
    public void validatePostLoad() {
        if (general == null) {
            LOGGER.warn("General section is missing, resetting it to default values");
            general = new General();
        }
        if (dropDelay == null) {
            LOGGER.warn("Drop Delay section is missing, resetting it to default values");
            dropDelay = new DropDelay();
        }
        if (favorites == null) {
            LOGGER.warn("Favorites section is missing, resetting it to default values");
            favorites = new Favorites();
        } else
            favorites.postLoad();
        FavoredChecker.updateFavoredSets(this);
    }
}
