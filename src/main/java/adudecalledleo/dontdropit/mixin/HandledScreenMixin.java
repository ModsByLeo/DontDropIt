package adudecalledleo.dontdropit.mixin;

import adudecalledleo.dontdropit.DontDropIt;
import adudecalledleo.dontdropit.DropDelayRenderer;
import adudecalledleo.dontdropit.ModKeyBindings;
import adudecalledleo.dontdropit.config.FavoredChecker;
import adudecalledleo.dontdropit.config.ModConfig;
import adudecalledleo.dontdropit.duck.HandledScreenHooks;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickWindowC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static adudecalledleo.dontdropit.ModKeyBindings.keyDropStack;

// TODO drop blocked tooltip, OOB click drop override, cursor close drop override
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements HandledScreenHooks {
    protected HandledScreenMixin() {
        super(null);
    }

    @Shadow @Final protected T handler;
    @Shadow @Final protected PlayerInventory playerInventory;
    @Shadow protected Slot focusedSlot;
    @Shadow protected abstract void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType);

    @Override
    public boolean canDrop() {
        if (client == null || client.player == null || focusedSlot == null)
            return false;
        // TODO Return null for creative slots, since those are always instant
        return focusedSlot.canTakeItems(client.player);
    }

    @Override
    public void drop(boolean entireStack) {
        if (!canDrop() || getSelectedStack().isEmpty())
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
        // TODO Creative slots shouldn't be delayed
        if (AutoConfig.getConfigHolder(ModConfig.class).getConfig().dropDelay.enabled)
            return false;
        return canDrop() && FavoredChecker.canDropStack(getSelectedStack()) && keyBinding.matchesKey(keyCode, scanCode);
    }

    @Redirect(method = "keyPressed",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;hasControlDown()Z"))
    public boolean useDropStackKey() {
        return ModKeyBindings.isDown(keyDropStack);
    }

    @Inject(method = "onClose", at = @At("HEAD"), cancellable = true)
    public void cursorCloseDropOverride(CallbackInfo ci) {
        if (client == null || client.player == null)
            return;
        ItemStack cursorStack = playerInventory.getCursorStack();
        boolean preventDrop = false;
        switch (AutoConfig.getConfigHolder(ModConfig.class).getConfig().general.cursorCloseDropOverride) {
        case FAVORITE_ITEMS:
            if (!FavoredChecker.canDropStack(cursorStack))
                break;
        case ALL_ITEMS:
            preventDrop = true;
        case DISABLED:
            break;
        }
        if (cursorStack.isEmpty() || !preventDrop)
            return;
        int targetSlot;
        targetSlot = playerInventory.getEmptySlot();
        DontDropIt.LOGGER.info("tried empty slot - got ID {}", targetSlot);
        if (targetSlot < 0) {
            targetSlot = playerInventory.getOccupiedSlotWithRoomForStack(cursorStack);
            DontDropIt.LOGGER.info("tried occupied slot - got ID {}", targetSlot);
        }
        if (targetSlot >= 0) {
            ci.cancel();
            // manually send the packets, so we can make sure the click action is received before the GUI closed action
            short actionId = handler.getNextActionId(playerInventory);
            ItemStack stack = handler.onSlotClick(targetSlot, 0, SlotActionType.PICKUP, client.player);
            client.player.networkHandler.getConnection().send(new ClickWindowC2SPacket(handler.syncId, targetSlot,
                    0, SlotActionType.PICKUP, stack, actionId), future ->
                    client.execute(() -> {
                        DontDropIt.LOGGER.info("Closing screen!");
                        client.player.closeHandledScreen();
                        client.openScreen(null); // relock the cursor
                    }));
        }
    }

    @Inject(method = "drawSlot",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
                     shift = At.Shift.AFTER))
    public void drawSlotProgressOverlay(MatrixStack matrixStack, Slot slot, CallbackInfo ci) {
        // TODO Don't do this for creative slots
        DropDelayRenderer.renderOverlay(matrixStack, slot.getStack(), slot.x, slot.y, getZOffset());
    }
}
