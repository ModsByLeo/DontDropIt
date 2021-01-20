package adudecalledleo.dontdropit.duck;

import net.minecraft.item.ItemStack;

public interface HandledScreenHooks {
    boolean dontdropit_canDrop();
    ItemStack dontdropit_getSelectedStack();
    void dontdropit_drop(boolean entireStack);
}
