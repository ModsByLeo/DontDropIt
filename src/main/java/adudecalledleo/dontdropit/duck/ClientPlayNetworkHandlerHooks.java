package adudecalledleo.dontdropit.duck;

import net.minecraft.item.ItemStack;

public interface ClientPlayNetworkHandlerHooks {
    void dontdropit_clickSlotAndClose(int syncId, int slotId, short actionId, ItemStack stack);
}
