package adudecalledleo.dontdropit.mixin.handledscreen;

import java.util.ArrayList;

import adudecalledleo.dontdropit.ModKeyBindings;
import adudecalledleo.dontdropit.config.DropBehaviorOverride;
import adudecalledleo.dontdropit.config.FavoredChecker;
import adudecalledleo.dontdropit.config.ModConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;

import static adudecalledleo.dontdropit.ModKeyBindings.keyForceDrop;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin_InterceptMouse<T extends ScreenHandler> extends Screen {
    @Shadow @Final protected T handler;
    @Shadow protected int x;
    @Shadow protected int y;

    @Shadow protected abstract void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType);
    @Shadow protected abstract boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button);

    private HandledScreenMixin_InterceptMouse() {
        super(Text.empty());
        throw new RuntimeException("Mixin constructor called");
    }

    @Unique
    private boolean shouldAllowShiftClick(Slot slot, int invSlot, SlotActionType actionType) {
        if (!ModConfig.get().favorites.disableShiftClick || actionType != SlotActionType.QUICK_MOVE) {
            return true;
        }
        if (slot == null && invSlot >= 0) {
            slot = handler.slots.get(invSlot);
        }
        if (slot == null) {
            return true;
        }
        return !FavoredChecker.isStackFavored(slot.getStack());
    }

    @Unique
    private void disableFavoredShiftClick(Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        if (shouldAllowShiftClick(slot, invSlot, actionType)) {
            onMouseClick(slot, invSlot, clickData, actionType);
        }
    }

    @Unique
    private boolean dontdropit$cancelOnMouseClick;

    @ModifyArg(method = "mouseClicked", index = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V",
                    ordinal = 1))
    public Slot disableFavoredShiftClick_mouseClicked(Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        dontdropit$cancelOnMouseClick = !shouldAllowShiftClick(slot, invSlot, actionType);
        return slot;
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V",
            at = @At("HEAD"),
            cancellable = true)
    public void cancelOnMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (dontdropit$cancelOnMouseClick) {
            dontdropit$cancelOnMouseClick = false;
            ci.cancel();
        }
    }

    @Redirect(method = "mouseReleased",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V",
                       ordinal = 0))
    public void disableFavoredShiftClick_mouseReleasedDouble(@SuppressWarnings("rawtypes") HandledScreen handledScreen,
            Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        disableFavoredShiftClick(slot, invSlot, clickData, actionType);
    }

    @Redirect(method = "mouseReleased",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V",
                       ordinal = 9))
    public void oobClickDropOverride(@SuppressWarnings("rawtypes") HandledScreen handledScreen,
            Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        if (actionType == SlotActionType.QUICK_MOVE) {
            disableFavoredShiftClick(slot, invSlot, clickData, actionType);
            return;
        }
        DropBehaviorOverride oobDropClickOverride = ModConfig.get().general.oobDropClickOverride;
        if (oobDropClickOverride == DropBehaviorOverride.DISABLED || invSlot != -999 || actionType != SlotActionType.PICKUP) {
            onMouseClick(slot, invSlot, clickData, actionType);
            return;
        }
        ItemStack cursorStack = handler.getCursorStack();
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

    @Inject(method = "drawMouseoverTooltip", at = @At("TAIL"))
    public void drawDropBlockTooltip(MatrixStack matrixStack, int mouseX, int mouseY, CallbackInfo ci) {
        ItemStack cursorStack = handler.getCursorStack();
        if (cursorStack.isEmpty() || !isClickOutsideBounds(mouseX, mouseY, x, y, 0))
            return;
        ArrayList<Text> tooltipTexts = new ArrayList<>();
        boolean forceDrop = ModKeyBindings.isDown(keyForceDrop);
        boolean canDrop = false;
        switch (ModConfig.get().general.oobDropClickOverride) {
        case FAVORITE_ITEMS:
            if (FavoredChecker.isStackFavored(cursorStack))
                break;
        case DISABLED:
            canDrop = true;
            break;
        }
        if (forceDrop || canDrop) {
            tooltipTexts.add(Text.translatable("dontdropit.tooltip.drop.allowed")
                    .styled(style -> style.withBold(true).withBold(true).withColor(Formatting.GREEN)));
            if (!canDrop) {
                tooltipTexts.add(Text.translatable("dontdropit.tooltip.drop.allowed.unblock_hint")
                        .styled(style -> style.withItalic(true).withColor(Formatting.GRAY)));
            }
        } else {
            tooltipTexts.add(Text.translatable("dontdropit.tooltip.drop.blocked")
                    .styled(style -> style.withBold(true).withColor(Formatting.RED)));
            if (keyForceDrop.isUnbound()) {
                tooltipTexts.add(Text.translatable("dontdropit.tooltip.drop.unblock_hint.unbound[0]",
                        Texts.bracketed(Text.translatable(keyForceDrop.getTranslationKey())
                                .styled(style -> style.withBold(true).withColor(Formatting.WHITE))))
                        .styled(style -> style.withColor(Formatting.GRAY)));
                tooltipTexts.add(Text.translatable("dontdropit.tooltip.drop.unblock_hint.unbound[1]")
                        .styled(style -> style.withColor(Formatting.GRAY)));
            } else
                tooltipTexts.add(Text.translatable("dontdropit.tooltip.drop.unblock_hint",
                        Texts.bracketed(Text.translatable(keyForceDrop.getBoundKeyTranslationKey())
                                .styled(style -> style.withBold(true).withColor(Formatting.WHITE))))
                        .styled(style -> style.withColor(Formatting.GRAY)));
        }
        renderTooltip(matrixStack, tooltipTexts, mouseX, mouseY);
    }
}
