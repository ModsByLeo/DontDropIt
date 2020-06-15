package adudecalledleo.dontdropit.api;

import net.minecraft.container.Slot;

public interface ContainerScreenExtensions {
    Slot getFocusedSlot();
    void drop(boolean entireStack);
}
