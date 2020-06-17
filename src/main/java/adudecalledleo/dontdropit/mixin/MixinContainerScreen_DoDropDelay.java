package adudecalledleo.dontdropit.mixin;

import adudecalledleo.dontdropit.DropHandler;
import adudecalledleo.dontdropit.api.ContainerScreenExtensions;
import adudecalledleo.dontdropit.config.ModConfig;
import adudecalledleo.dontdropit.config.ModConfigHolder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ContainerScreen.class)
public abstract class MixinContainerScreen_DoDropDelay extends Screen implements ContainerScreenExtensions {
    protected MixinContainerScreen_DoDropDelay() {
        super(null);
        throw new RuntimeException("This shouldn't be invoked...");
    }

    @Shadow(prefix = "dontdropit$")
    protected abstract void dontdropit$onMouseClick(Slot slot, int invSlot, int button, SlotActionType slotActionType);

    @Override
    @Accessor
    public abstract Slot getFocusedSlot();

    @Override
    public void drop(boolean entireStack) {
        if (getFocusedSlot() == null)
            return;
        dontdropit$onMouseClick(getFocusedSlot(), getFocusedSlot().id, entireStack ? 0 : 1, SlotActionType.THROW);
    }

    @Redirect(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/ContainerScreen;onMouseClick(Lnet/minecraft/container/Slot;IILnet/minecraft/container/SlotActionType;)V",
            ordinal = 1))
    public void dontdropit$disableDropKey(ContainerScreen containerScreen, Slot slot, int invSlot, int button, SlotActionType slotActionType) {
        if (!ModConfigHolder.getConfig().dropDelay.enabled)
            dontdropit$onMouseClick(slot, invSlot, button, slotActionType);
    }

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/ContainerScreen;onMouseClick(Lnet/minecraft/container/Slot;IILnet/minecraft/container/SlotActionType;)V",
            ordinal = 1))
    public void dontdropit$disableDropOOBClick(ContainerScreen containerScreen, Slot slot, int invSlot, int button, SlotActionType slotActionType) {
        if (invSlot == -999 && slotActionType == SlotActionType.THROW) {
            if (ModConfigHolder.getConfig().general.disableOOBClickDrop)
                return;
        }
        dontdropit$onMouseClick(slot, invSlot, button, slotActionType);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;colorMask(ZZZZ)V",
            ordinal = 1),
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void dontdropit$renderDropProgress(int mouseX, int mouseY, float delta, CallbackInfo ci, int i, int j, int k,
                                              int l, int m, Slot slot) {
        if (!ModConfigHolder.getConfig().dropDelay.enabled)
            return;
        if (InputUtil.isKeyPressed(minecraft.getWindow().getHandle(),
                KeyBindingHelper.getBoundKeyOf(minecraft.options.keyDrop).getKeyCode())) {
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0, 0, getBlitOffset());
            DropHandler.renderSlotProgressOverlay(slot);
            RenderSystem.popMatrix();
        }
    }

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/ContainerScreen;setBlitOffset(I)V",
            ordinal = 1))
    public void dontdropit$renderFavoriteIcon(Slot slot, CallbackInfo ci) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0, 0, getBlitOffset());
        RenderSystem.enableBlend();
        DropHandler.renderSlotFavoriteIcon(slot);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.popMatrix();
    }
}
