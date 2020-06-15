package adudecalledleo.dontdropit.api;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemStack;

public class ContainerScreenDropHandlerInterface implements DropHandlerInterface {
    private final ContainerScreenExtensions cse;

    public ContainerScreenDropHandlerInterface(ContainerScreenExtensions cse) {
        this.cse = cse;
    }

    public ContainerScreenExtensions getCse() {
        return cse;
    }

    @Override
    public boolean isDropKeyDown(MinecraftClient mc) {
        return InputUtil.isKeyPressed(mc.getWindow().getHandle(),
                KeyBindingHelper.getBoundKeyOf(mc.options.keyDrop).getKeyCode());
    }

    @Override
    public ItemStack getCurrentStack(MinecraftClient mc) {
        Slot focusedSlot = cse.getFocusedSlot();
        if (focusedSlot == null || !focusedSlot.hasStack())
            return ItemStack.EMPTY;
        return focusedSlot.getStack();
    }

    @Override
    public void drop(boolean entireStack, MinecraftClient mc) {
        cse.drop(entireStack);
    }
}
