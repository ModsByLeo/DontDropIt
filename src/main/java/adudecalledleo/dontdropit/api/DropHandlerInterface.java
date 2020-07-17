package adudecalledleo.dontdropit.api;

import adudecalledleo.dontdropit.DontDropItMod;
import adudecalledleo.dontdropit.util.FavoritesUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

public interface DropHandlerInterface {
    boolean isKeyDown(KeyBinding keyBinding, MinecraftClient mc);
    ItemStack getCurrentStack(MinecraftClient mc);
    void drop(boolean entireStack, MinecraftClient mc);

    @SuppressWarnings("ConstantConditions")
    default boolean canDropStack(ItemStack stack, MinecraftClient mc) {
        if (!mc.player.isCreative() && stack.getItem() instanceof ArmorItem && EnchantmentHelper.hasBindingCurse(stack))
            for (ItemStack armorStack : mc.player.getArmorItems())
                if (stack == armorStack)
                    return false;
        return isKeyDown(DontDropItMod.keyForceDrop, mc) || !FavoritesUtil.isStackFavorite(stack);
    }
}
