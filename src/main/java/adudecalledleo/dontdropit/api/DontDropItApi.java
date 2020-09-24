package adudecalledleo.dontdropit.api;

import net.minecraft.screen.slot.Slot;

import java.util.Set;

public interface DontDropItApi {
    /**
     * Slots that have classes that are {@linkplain Class#isAssignableFrom(Class) assignable from} any of the classes in
     * the returned set will always drop instantly upon the user pressing the drop button.<p>
     * Useful for "ghost" slots which repurpose the drop action for clearing.
     *
     * @return the set of classes to ignore drop delay on
     */
    Set<Class<? extends Slot>> getIgnoredDropDelaySlots();
}
