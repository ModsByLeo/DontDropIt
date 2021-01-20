package adudecalledleo.dontdropit.mixin.handledscreen;

import adudecalledleo.dontdropit.ModKeyBindings;
import adudecalledleo.dontdropit.config.DropBehaviorOverride;
import adudecalledleo.dontdropit.config.FavoredChecker;
import adudecalledleo.dontdropit.config.ModConfig;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin_InterceptMouse<T extends ScreenHandler> {
    @Shadow @Final protected T handler;
    @Shadow @Final protected PlayerInventory playerInventory;

    @Shadow protected abstract void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType);

    @Unique
    private void disableFavoredShiftClick(Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        if (!ModConfig.get().favorites.disableShiftClick || actionType != SlotActionType.QUICK_MOVE) {
            onMouseClick(slot, invSlot, clickData, actionType);
            return;
        }
        if (slot == null && invSlot >= 0)
            slot = handler.slots.get(invSlot);
        if (slot == null || !FavoredChecker.isStackFavored(slot.getStack()))
            onMouseClick(slot, invSlot, clickData, actionType);
    }

    @Redirect(method = "mouseClicked",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V",
                       ordinal = 1))
    public void disableFavoredShiftClick_mouseClicked(@SuppressWarnings("rawtypes") HandledScreen handledScreen,
            Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        disableFavoredShiftClick(slot, invSlot, clickData, actionType);
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
}
