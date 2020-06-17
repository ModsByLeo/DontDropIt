package adudecalledleo.dontdropit.api;

import adudecalledleo.dontdropit.DontDropItMod;
import adudecalledleo.dontdropit.util.ConfigUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

public interface DropHandlerInterface {
    boolean isKeyDown(KeyBinding keyBinding, MinecraftClient mc);
    ItemStack getCurrentStack(MinecraftClient mc);
    void drop(boolean entireStack, MinecraftClient mc);
    default boolean canDropStack(ItemStack stack, MinecraftClient mc) {
        if (stack.getItem() instanceof ArmorItem && EnchantmentHelper.hasBindingCurse(stack))
            for (ItemStack armorStack : mc.player.getArmorItems())
                if (stack == armorStack)
                    return false;
        return isKeyDown(DontDropItMod.keyForceDrop, mc) || !ConfigUtil.isStackFavorite(stack);
    }
}
