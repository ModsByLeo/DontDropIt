package adudecalledleo.dontdropit.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

public interface DropHandlerInterface {
    boolean isDropKeyDown(MinecraftClient mc);
    ItemStack getCurrentStack(MinecraftClient mc);
    void drop(boolean entireStack, MinecraftClient mc);
    default boolean canDropStack(ItemStack stack, MinecraftClient mc) {
        if (mc.player.isCreative())
            return true;
        if (stack.getItem() instanceof ArmorItem && EnchantmentHelper.hasBindingCurse(stack))
            for (ItemStack armorStack : mc.player.getArmorItems())
                if (stack == armorStack)
                    return false;
        return true;
    }
}
