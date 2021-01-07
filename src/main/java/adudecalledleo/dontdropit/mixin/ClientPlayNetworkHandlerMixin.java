package adudecalledleo.dontdropit.mixin;

import adudecalledleo.dontdropit.duck.ClientPlayNetworkHandlerHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.ConfirmScreenActionS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin implements ClientPlayNetworkHandlerHooks {
    @Shadow public abstract void sendPacket(Packet<?> packet);

    @Shadow private MinecraftClient client;
    @Unique private short waitingForActionId = -1;
    @Unique private int waitingForSyncId = -1;

    @Override
    public void clickSlotAndClose(int syncId, int slotId, short actionId, ItemStack stack) {
        sendPacket(new ClickSlotC2SPacket(syncId, slotId, 0, SlotActionType.PICKUP, stack, actionId));
        waitingForActionId = actionId;
        waitingForSyncId = syncId;
        client.openScreen(null); // relock mouse
    }

    @Inject(method = "onConfirmScreenAction", at = @At("TAIL"))
    public void closeOnConfirmAction(ConfirmScreenActionS2CPacket packet, CallbackInfo ci) {
        if (!packet.wasAccepted())
            return;
        if (waitingForSyncId == packet.getSyncId() && waitingForActionId == packet.getActionId()) {
            waitingForActionId = -1;
            waitingForSyncId = -1;
            if (client.player != null)
                client.player.closeHandledScreen();
        }
    }
}
