package adudecalledleo.dontdropit.api;

import net.minecraft.screen.slot.Slot;

public interface ContainerScreenExtensions {
    Slot getFocusedSlot();
    void drop(boolean entireStack);
}
