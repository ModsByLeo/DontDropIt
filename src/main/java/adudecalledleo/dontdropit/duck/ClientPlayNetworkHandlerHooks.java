package adudecalledleo.dontdropit.duck;

import net.minecraft.item.ItemStack;

public interface ClientPlayNetworkHandlerHooks {
    void clickSlotAndClose(int syncId, int slotId, short actionId, ItemStack stack);
}
