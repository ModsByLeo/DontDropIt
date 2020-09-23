package adudecalledleo.dontdropit.duck;

import net.minecraft.item.ItemStack;

public interface HandledScreenHooks {
    boolean canDrop();
    ItemStack getSelectedStack();
    void drop(boolean entireStack);
}
