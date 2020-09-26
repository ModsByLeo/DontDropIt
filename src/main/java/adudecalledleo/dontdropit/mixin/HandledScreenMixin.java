package adudecalledleo.dontdropit.mixin;

import adudecalledleo.dontdropit.DropDelayRenderer;
import adudecalledleo.dontdropit.IgnoredSlots;
import adudecalledleo.dontdropit.ModKeyBindings;
import adudecalledleo.dontdropit.config.DropBehaviorOverride;
import adudecalledleo.dontdropit.config.FavoredChecker;
import adudecalledleo.dontdropit.config.ModConfig;
import adudecalledleo.dontdropit.duck.ClientPlayNetworkHandlerHooks;
import adudecalledleo.dontdropit.duck.HandledScreenHooks;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

import static adudecalledleo.dontdropit.ModKeyBindings.keyDropStack;
import static adudecalledleo.dontdropit.ModKeyBindings.keyForceDrop;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements HandledScreenHooks {
    protected HandledScreenMixin() {
        super(null);
    }

    @Shadow @Final protected T handler;
    @Shadow @Final protected PlayerInventory playerInventory;
    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow protected Slot focusedSlot;

    @Shadow protected abstract void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType);
    @Shadow protected abstract boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button);

    @Override
    public boolean canDrop() {
        if (client == null || client.player == null || focusedSlot == null)
            return false;
        if (IgnoredSlots.isSlotIgnored(focusedSlot))
            return false;
        return focusedSlot.canTakeItems(client.player);
    }

    @Override
    public void drop(boolean entireStack) {
        if (focusedSlot == null || getSelectedStack().isEmpty())
            return;
        onMouseClick(focusedSlot, focusedSlot.id, entireStack ? 1 : 0, SlotActionType.THROW);
    }

    @Override
    public ItemStack getSelectedStack() {
        return focusedSlot == null ? ItemStack.EMPTY : focusedSlot.getStack();
    }

    @Redirect(method = "keyPressed",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/options/KeyBinding;matchesKey(II)Z",
                       ordinal = 2))
    public boolean disableDropKey(KeyBinding keyBinding, int keyCode, int scanCode) {
        if (focusedSlot == null || IgnoredSlots.isSlotIgnored(focusedSlot))
            return keyBinding.matchesKey(keyCode, scanCode);
        if (AutoConfig.getConfigHolder(ModConfig.class).getConfig().dropDelay.enabled)
            return false;
        return canDrop() && FavoredChecker.canDropStack(getSelectedStack()) && keyBinding.matchesKey(keyCode, scanCode);
    }

    @Redirect(method = "keyPressed",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;hasControlDown()Z"))
    public boolean useDropStackKey() {
        return ModKeyBindings.isDown(keyDropStack);
    }

    /*
    // debug inject that traces click events and fires a stack trace when an OOB drop click event is detected
    @Inject(method = "onMouseClick", at = @At("HEAD"))
    public void traceCursorDrop(Slot slot, int invSlot, int clickData, SlotActionType actionType, CallbackInfo ci) {
        DontDropIt.LOGGER.info("onMouseClick: slot = {}, invSlot = {}, clickData = {}, actionType = {}",
                slot, invSlot, clickData, actionType);
        if (slot == null && invSlot == -999 && actionType == SlotActionType.PICKUP) {
            // there's probably a better way to do this but this isn't going into production so W/E
            try {
                throw new RuntimeException("Generated stack trace");
            } catch (RuntimeException e) {
                DontDropIt.LOGGER.info("onMouseClick: OOB drop logged!", e);
            }
        }
    }
     */

    @Redirect(method = "mouseReleased",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V",
                       ordinal = 9))
    public void oobClickDropOverride(@SuppressWarnings("rawtypes") HandledScreen handledScreen,
            Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        DropBehaviorOverride oobDropClickOverride = AutoConfig.getConfigHolder(ModConfig.class).getConfig().general.oobDropClickOverride;
        if (oobDropClickOverride == DropBehaviorOverride.DISABLED || invSlot != -999 || actionType != SlotActionType.PICKUP) {
            onMouseClick(slot, invSlot, clickData, actionType);
            return;
        }
        ItemStack cursorStack = playerInventory.getCursorStack();
        boolean forceDrop = ModKeyBindings.isDown(ModKeyBindings.keyForceDrop);
        boolean canDrop = true;
        switch (oobDropClickOverride) {
        case FAVORITE_ITEMS:
            if (!FavoredChecker.isStackFavored(cursorStack))
                break;
        case ALL_ITEMS:
            canDrop = false;
            break;
        }
        if (forceDrop || canDrop)
            onMouseClick(null, -999, clickData, SlotActionType.PICKUP);
    }

    @Inject(method = "onClose", at = @At("HEAD"), cancellable = true)
    public void cursorCloseDropOverride(CallbackInfo ci) {
        if (client == null || client.player == null)
            return;
        ItemStack cursorStack = playerInventory.getCursorStack();
        boolean canDrop = true;
        switch (AutoConfig.getConfigHolder(ModConfig.class).getConfig().general.cursorCloseDropOverride) {
        case FAVORITE_ITEMS:
            if (!FavoredChecker.isStackFavored(cursorStack))
                break;
        case ALL_ITEMS:
            canDrop = false;
            break;
        }
        if (cursorStack.isEmpty() || canDrop)
            return;
        int targetSlot;
        targetSlot = playerInventory.getEmptySlot();
        if (targetSlot < 0)
            targetSlot = playerInventory.getOccupiedSlotWithRoomForStack(cursorStack);
        if (targetSlot >= 0) {
            // locate handler slot ID that matches the target inventory slot ID
            int slotId = -1;
            for (Slot slot : handler.slots) {
                if (slot.inventory == playerInventory && ((SlotAccessor) slot).getInventoryIndex() == targetSlot) {
                    slotId = slot.id;
                    break;
                }
            }
            if (slotId < 0)
                return;
            short actionId = handler.getNextActionId(playerInventory);
            ItemStack stack = handler.onSlotClick(slotId, 0, SlotActionType.PICKUP, client.player);
            ci.cancel();
            ((ClientPlayNetworkHandlerHooks) client.player.networkHandler)
                    .clickSlotAndClose(handler.syncId, slotId, actionId, stack);
        }
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

    @Inject(method = "drawMouseoverTooltip", at = @At("TAIL"))
    public void drawDropBlockTooltip(MatrixStack matrixStack, int mouseX, int mouseY, CallbackInfo ci) {
        ItemStack cursorStack = playerInventory.getCursorStack();
        if (cursorStack.isEmpty() || !isClickOutsideBounds(mouseX, mouseY, x, y, 0))
            return;
        ArrayList<Text> tooltipTexts = new ArrayList<>();
        boolean forceDrop = ModKeyBindings.isDown(keyForceDrop);
        boolean canDrop = false;
        switch (AutoConfig.getConfigHolder(ModConfig.class).getConfig().general.oobDropClickOverride) {
        case FAVORITE_ITEMS:
            if (FavoredChecker.isStackFavored(cursorStack))
                break;
        case DISABLED:
            canDrop = true;
            break;
        }
        if (forceDrop || canDrop) {
            tooltipTexts.add(new TranslatableText("dontdropit.tooltip.drop.allowed")
                    .styled(style -> style.withBold(true).withBold(true).withColor(Formatting.GREEN)));
            if (!canDrop)
                tooltipTexts.add(new TranslatableText("dontdropit.tooltip.drop.allowed.unblock_hint")
                        .styled(style -> style.withItalic(true).withColor(Formatting.GRAY)));
        } else {
            tooltipTexts.add(new TranslatableText("dontdropit.tooltip.drop.blocked")
                    .styled(style -> style.withBold(true).withColor(Formatting.RED)));
            if (keyForceDrop.isUnbound()) {
                tooltipTexts.add(new TranslatableText("dontdropit.tooltip.drop.unblock_hint.unbound[0]",
                        Texts.bracketed(new TranslatableText(keyForceDrop.getTranslationKey())
                                .styled(style -> style.withBold(true).withColor(Formatting.WHITE))))
                        .styled(style -> style.withColor(Formatting.GRAY)));
                tooltipTexts.add(new TranslatableText("dontdropit.tooltip.drop.unblock_hint.unbound[1]")
                        .styled(style -> style.withColor(Formatting.GRAY)));
            } else
                tooltipTexts.add(new TranslatableText("dontdropit.tooltip.drop.unblock_hint",
                        Texts.bracketed(new TranslatableText(keyForceDrop.getBoundKeyTranslationKey())
                                .styled(style -> style.withBold(true).withColor(Formatting.WHITE))))
                        .styled(style -> style.withColor(Formatting.GRAY)));
        }
        renderTooltip(matrixStack, tooltipTexts, mouseX, mouseY);
    }
}
