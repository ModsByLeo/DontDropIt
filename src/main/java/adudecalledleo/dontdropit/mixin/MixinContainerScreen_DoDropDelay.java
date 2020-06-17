package adudecalledleo.dontdropit.mixin;

import adudecalledleo.dontdropit.DontDropItMod;
import adudecalledleo.dontdropit.DropHandler;
import adudecalledleo.dontdropit.api.ContainerScreenExtensions;
import adudecalledleo.dontdropit.config.ModConfigHolder;
import adudecalledleo.dontdropit.util.ConfigUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

@Mixin(HandledScreen.class)
public abstract class MixinContainerScreen_DoDropDelay extends Screen implements ContainerScreenExtensions {
    protected MixinContainerScreen_DoDropDelay() {
        super(null);
        throw new RuntimeException("This shouldn't be invoked...");
    }

    @Shadow(prefix = "dontdropit$")
    protected abstract void dontdropit$onMouseClick(Slot slot, int invSlot, int button, SlotActionType slotActionType);

    @Shadow(prefix = "dontdropit$")
    protected abstract boolean dontdropit$isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button);

    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow @Final protected PlayerInventory playerInventory;

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "onMouseClick", at = @At("HEAD"), cancellable = true)
    public void dontdropit$disableOOBClickDrop(Slot slot, int invSlot, int button, SlotActionType slotActionType, CallbackInfo ci) {
        if (slot == null && invSlot == -999 && slotActionType == SlotActionType.PICKUP) {
            switch (ModConfigHolder.getConfig().general.oobDropClickOverride) {
            case FAVORITE_ITEMS:
                if (!ConfigUtil.isStackFavorite(playerInventory.getCursorStack()))
                    break;
            case ALL_ITEMS:
                if (InputUtil.isKeyPressed(minecraft.getWindow().getHandle(),
                        KeyBindingHelper.getBoundKeyOf(DontDropItMod.keyForceDrop).getCode()))
                    break;
                ci.cancel();
                break;
            case DISABLED:
                break;
            }
        }
    }

    @Override
    @Accessor
    public abstract Slot getFocusedSlot();

    @Override
    public void drop(boolean entireStack) {
        if (getFocusedSlot() == null)
            return;
        dontdropit$onMouseClick(getFocusedSlot(), getFocusedSlot().id, entireStack ? 0 : 1, SlotActionType.THROW);
    }

    @SuppressWarnings("rawtypes")
    @Redirect(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/ContainerScreen;onMouseClick(Lnet/minecraft/container/Slot;IILnet/minecraft/container/SlotActionType;)V",
            ordinal = 1))
    public void dontdropit$disableDropKey(HandledScreen containerScreen, Slot slot, int invSlot, int button, SlotActionType slotActionType) {
        if (slot instanceof CreativeInventoryScreen.LockableSlot || !ModConfigHolder.getConfig().dropDelay.enabled)
            dontdropit$onMouseClick(slot, invSlot, button, slotActionType);
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;colorMask(ZZZZ)V",
            ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    public void dontdropit$renderDropProgress(int mouseX, int mouseY, float delta, CallbackInfo ci, int i, int j, int k,
                                              int l, int m, Slot slot) {
        if (slot instanceof CreativeInventoryScreen.LockableSlot || !ModConfigHolder.getConfig().dropDelay.enabled)
            return;
        if (InputUtil.isKeyPressed(minecraft.getWindow().getHandle(),
                KeyBindingHelper.getBoundKeyOf(minecraft.options.keyDrop).getCode())) {
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0, 0, getBlitOffset() + 1);
            DropHandler.renderSlotProgressOverlay(slot);
            RenderSystem.popMatrix();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableDepthTest()V",
            ordinal = 1))
    public void dontdropit$renderDropTooltip(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!playerInventory.getCursorStack().isEmpty() && dontdropit$isClickOutsideBounds(mouseX, mouseY, x, y, 0)) {
            boolean blocked = false;
            if (!(((Object) this) instanceof CreativeInventoryScreen)) {
                blocked = !InputUtil.isKeyPressed(minecraft.getWindow().getHandle(),
                    KeyBindingHelper.getBoundKeyOf(DontDropItMod.keyForceDrop).getCode());
                switch (ModConfigHolder.getConfig().general.oobDropClickOverride) {
                case FAVORITE_ITEMS:
                    if (ConfigUtil.isStackFavorite(playerInventory.getCursorStack()))
                        break;
                case DISABLED:
                    blocked = false;
                    break;
                }
            }
            List<String> tooltipText = new ArrayList<>();
            if (blocked) {
                tooltipText.add(Formatting.RED.toString() + Formatting.BOLD.toString() + I18n.translate("dontdropit.tooltip.drop.blocked"));
                tooltipText.add(Formatting.GRAY.toString() + I18n.translate("dontdropit.tooltip.drop.unblock_hint", DontDropItMod.keyForceDrop.getBoundKeyLocalizedText()));
            } else
                tooltipText.add(I18n.translate("dontdropit.tooltip.drop.allowed"));
            renderTooltip(tooltipText, mouseX, mouseY);
        }
    }

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/ContainerScreen;setBlitOffset(I)V",
            ordinal = 1))
    public void dontdropit$renderFavoriteIcon(Slot slot, CallbackInfo ci) {
        if (slot instanceof CreativeInventoryScreen.LockableSlot)
            return;
        RenderSystem.disableDepthTest();
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0, 0, getBlitOffset());
        RenderSystem.enableBlend();
        DropHandler.renderSlotFavoriteIcon(slot);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.popMatrix();
    }
}
