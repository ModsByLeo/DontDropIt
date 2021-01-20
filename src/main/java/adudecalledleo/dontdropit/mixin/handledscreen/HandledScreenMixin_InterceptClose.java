package adudecalledleo.dontdropit.mixin.handledscreen;

import adudecalledleo.dontdropit.config.FavoredChecker;
import adudecalledleo.dontdropit.config.ModConfig;
import adudecalledleo.dontdropit.duck.ClientPlayNetworkHandlerHooks;
import adudecalledleo.dontdropit.mixin.SlotAccessor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin_InterceptClose<T extends ScreenHandler> extends Screen {
    @Shadow @Final protected T handler;
    @Shadow @Final protected PlayerInventory playerInventory;

    private HandledScreenMixin_InterceptClose() {
        super(LiteralText.EMPTY);
        throw new RuntimeException("Mixin constructor called");
    }

    @Inject(method = "onClose", at = @At("HEAD"), cancellable = true)
    public void cursorCloseDropOverride(CallbackInfo ci) {
        if (client == null || client.player == null)
            return;
        ItemStack cursorStack = playerInventory.getCursorStack();
        boolean canDrop = true;
        switch (ModConfig.get().general.cursorCloseDropOverride) {
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
                    .dontdropit_clickSlotAndClose(handler.syncId, slotId, actionId, stack);
        }
    }
}
