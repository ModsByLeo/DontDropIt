package adudecalledleo.dontdropit.api;

import net.minecraft.screen.slot.Slot;

public interface HandledScreenExtensions {
    Slot getFocusedSlot();
    void drop(boolean entireStack);
}
