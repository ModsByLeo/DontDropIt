package adudecalledleo.dontdropit.mixin;

import adudecalledleo.dontdropit.DropHandler;
import adudecalledleo.dontdropit.config.ModConfigHolder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud_RenderDropProgress extends DrawableHelper {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "renderHotbarItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;II)V"))
    public void dontdropit$renderDropProgress(int i, int j, float f, PlayerEntity playerEntity, ItemStack itemStack, CallbackInfo ci) {
        if (!ModConfigHolder.getConfig().dropDelay.enabled)
            return;
        if (InputUtil.isKeyPressed(client.getWindow().getHandle(),
                KeyBindingHelper.getBoundKeyOf(client.options.keyDrop).getKeyCode())) {
            if (itemStack == playerEntity.inventory.getMainHandStack()) {
                RenderSystem.pushMatrix();
                RenderSystem.translatef(0, 0, getBlitOffset());
                DropHandler.renderSlotUnderlay(i, j, 16, 16);
                RenderSystem.popMatrix();
            }
        }
    }
}
