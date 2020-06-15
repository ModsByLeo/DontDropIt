package adudecalledleo.dontdropit.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

public interface DropHandlerInterface {
    boolean isDropKeyDown(MinecraftClient mc);
    ItemStack getCurrentStack(MinecraftClient mc);
    void drop(boolean entireStack, MinecraftClient mc);
}
