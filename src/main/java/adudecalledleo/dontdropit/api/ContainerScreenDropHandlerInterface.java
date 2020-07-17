package adudecalledleo.dontdropit.api;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class ContainerScreenDropHandlerInterface implements DropHandlerInterface {
    private final ContainerScreenExtensions cse;

    public ContainerScreenDropHandlerInterface(ContainerScreenExtensions cse) {
        this.cse = cse;
    }

    public ContainerScreenExtensions getCse() {
        return cse;
    }

    @Override
    public boolean isKeyDown(KeyBinding keyBinding, MinecraftClient mc) {
        // KeyBindings aren't updated when in a Screen, so we need to manually check the key's state
        return InputUtil.isKeyPressed(mc.getWindow().getHandle(),
                KeyBindingHelper.getBoundKeyOf(keyBinding).getCode());
    }

    @Override
    public ItemStack getCurrentStack(MinecraftClient mc) {
        Slot focusedSlot = cse.getFocusedSlot();
        if (focusedSlot == null || focusedSlot instanceof CreativeInventoryScreen.LockableSlot || !focusedSlot.hasStack())
            return ItemStack.EMPTY;
        return focusedSlot.getStack();
    }

    @Override
    public void drop(boolean entireStack, MinecraftClient mc) {
        cse.drop(entireStack);
    }
}
