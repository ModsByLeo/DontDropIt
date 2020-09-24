package adudecalledleo.dontdropit;

import adudecalledleo.dontdropit.api.DontDropItApi;
import adudecalledleo.lionutils.InitializerUtil;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.screen.slot.Slot;

import java.util.List;

public class IgnoredSlots {
    private IgnoredSlots() {
        InitializerUtil.initCtor();
    }

    private static final ReferenceSet<Class<? extends Slot>> IGNORED_SLOTS = new ReferenceLinkedOpenHashSet<>();
    private static final Reference2BooleanMap<Class<? extends Slot>> CLASS_2_STATE_MAP = new Reference2BooleanOpenHashMap<>();

    static void collectFromEntrypoints() {
        List<DontDropItApi> entrypoints = FabricLoader.getInstance().getEntrypoints("dontdropit", DontDropItApi.class);
        for (DontDropItApi entrypoint : entrypoints)
            IGNORED_SLOTS.addAll(entrypoint.getIgnoredDropDelaySlots());
        IGNORED_SLOTS.remove(Slot.class); // don't act dumb
        CLASS_2_STATE_MAP.put(Slot.class, false); // ^
    }

    private static boolean isClassIgnored0(Class<? extends Slot> slotClass) {
        for (Class<? extends Slot> ignoredClass : IGNORED_SLOTS) {
            if (ignoredClass.isAssignableFrom(slotClass))
                return true;
        }
        return false;
    }

    public static boolean isClassIgnored(Class<? extends Slot> slotClass) {
        return CLASS_2_STATE_MAP.computeBooleanIfAbsent(slotClass, IgnoredSlots::isClassIgnored0);
    }

    public static boolean isSlotIgnored(Slot slot) {
        return isClassIgnored(slot.getClass());
    }
}
