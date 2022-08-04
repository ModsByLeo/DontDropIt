package adudecalledleo.dontdropit.mixin.handledscreen;

import adudecalledleo.dontdropit.config.FavoredChecker;
import adudecalledleo.dontdropit.config.ModConfig;
import adudecalledleo.dontdropit.mixin.KeyBindingAccessor;
import adudecalledleo.dontdropit.mixin.SlotAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin_InterceptClose<T extends ScreenHandler> extends Screen {
    @Shadow @Final protected T handler;

    @Shadow protected abstract void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType);

    private HandledScreenMixin_InterceptClose() {
        super(Text.empty());
        throw new RuntimeException("Mixin constructor called");
    }

    @Unique private PlayerInventory playerInventory;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void capturePlayerInventory(T handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        this.playerInventory = inventory;
    }

    @Inject(method = "close", at = @At("HEAD"))
    public void cursorCloseDropOverride(CallbackInfo ci) {
        if (client == null || client.player == null)
            return;
        // eat all drop key presses, so we don't drop hotbar items if drop delay is disabled
        client.options.dropKey.setPressed(false);
        ((KeyBindingAccessor) client.options.dropKey).setTimesPressed(0);

        ItemStack cursorStack = handler.getCursorStack();
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
        int targetInvId;
        targetInvId = playerInventory.getEmptySlot();
        if (targetInvId < 0)
            targetInvId = playerInventory.getOccupiedSlotWithRoomForStack(cursorStack);
        if (targetInvId >= 0) {
            // locate handler slot ID that matches the target inventory slot ID
            Slot targetSlot = null;
            for (Slot slot : handler.slots) {
                if (slot.inventory == playerInventory && ((SlotAccessor) slot).getInventoryIndex() == targetInvId) {
                    targetSlot = slot;
                    break;
                }
            }
            if (targetSlot == null)
                return;
            onMouseClick(targetSlot, -1, 0, SlotActionType.PICKUP);
        }
    }
}
