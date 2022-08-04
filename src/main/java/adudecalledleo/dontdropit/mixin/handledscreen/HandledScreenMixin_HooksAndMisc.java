package adudecalledleo.dontdropit.mixin.handledscreen;

import adudecalledleo.dontdropit.DropDelayRenderer;
import adudecalledleo.dontdropit.IgnoredSlots;
import adudecalledleo.dontdropit.ModKeyBindings;
import adudecalledleo.dontdropit.config.FavoredChecker;
import adudecalledleo.dontdropit.config.ModConfig;
import adudecalledleo.dontdropit.duck.HandledScreenHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import static adudecalledleo.dontdropit.ModKeyBindings.keyDropStack;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin_HooksAndMisc extends Screen implements HandledScreenHooks {
    @Shadow protected Slot focusedSlot;

    @Shadow protected abstract void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType);

    private HandledScreenMixin_HooksAndMisc() {
        super(Text.empty());
        throw new RuntimeException("Mixin constructor called");
    }

    @Override
    public boolean dontdropit_canDrop() {
        if (client == null || client.player == null || focusedSlot == null)
            return false;
        if (IgnoredSlots.isSlotIgnored(focusedSlot))
            return false;
        return focusedSlot.canTakeItems(client.player);
    }

    @Override
    public void dontdropit_drop(boolean entireStack) {
        if (focusedSlot == null || dontdropit_getSelectedStack().isEmpty())
            return;
        onMouseClick(focusedSlot, focusedSlot.id, entireStack ? 1 : 0, SlotActionType.THROW);
    }

    @Override
    public ItemStack dontdropit_getSelectedStack() {
        return focusedSlot == null ? ItemStack.EMPTY : focusedSlot.getStack();
    }

    @Redirect(method = "keyPressed",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;matchesKey(II)Z",
                       ordinal = 2))
    public boolean disableDropKey(KeyBinding keyBinding, int keyCode, int scanCode) {
        if (focusedSlot == null || IgnoredSlots.isSlotIgnored(focusedSlot))
            return keyBinding.matchesKey(keyCode, scanCode);
        ItemStack stack = dontdropit_getSelectedStack();
        if (ModConfig.get().dropDelay.mode.isEnabled(stack))
            return false;
        return dontdropit_canDrop()
                && FavoredChecker.canDropStack(dontdropit_getSelectedStack())
                && keyBinding.matchesKey(keyCode, scanCode);
    }

    @Redirect(method = "keyPressed",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;hasControlDown()Z"))
    public boolean useDropStackKey() {
        return ModKeyBindings.isDown(keyDropStack);
    }

    @Inject(method = "drawSlot",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
                     shift = At.Shift.AFTER))
    public void drawSlotProgressOverlay(MatrixStack matrixStack, Slot slot, CallbackInfo ci) {
        if (IgnoredSlots.isSlotIgnored(slot))
            return;
        DropDelayRenderer.renderOverlay(matrixStack, slot.getStack(), slot.x, slot.y, getZOffset());
    }
}
