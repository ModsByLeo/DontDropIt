package adudecalledleo.dontdropit.mixin;

import adudecalledleo.dontdropit.DontDropItMod;
import adudecalledleo.dontdropit.DropHandler;
import adudecalledleo.dontdropit.api.HandledScreenExtensions;
import adudecalledleo.dontdropit.config.ModConfigHolder;
import adudecalledleo.dontdropit.util.ConfigUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
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
public abstract class MixinHandledScreen_DoDropDelay extends Screen implements HandledScreenExtensions {
    protected MixinHandledScreen_DoDropDelay() {
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
                if (InputUtil.isKeyPressed(client.getWindow().getHandle(),
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
    @Redirect(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V",
            ordinal = 1))
    public void dontdropit$disableDropKey(HandledScreen containerScreen, Slot slot, int invSlot, int button, SlotActionType slotActionType) {
        if (slot instanceof CreativeInventoryScreen.LockableSlot || !ModConfigHolder.getConfig().dropDelay.enabled)
            dontdropit$onMouseClick(slot, invSlot, button, slotActionType);
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;colorMask(ZZZZ)V",
            ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    public void dontdropit$renderDropProgress(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci,
                                              int i, int j, int k, int l, int m, Slot slot) {
        if (slot instanceof CreativeInventoryScreen.LockableSlot || !ModConfigHolder.getConfig().dropDelay.enabled)
            return;
        if (InputUtil.isKeyPressed(client.getWindow().getHandle(),
                KeyBindingHelper.getBoundKeyOf(client.options.keyDrop).getCode())) {
            matrices.push();
            matrices.translate(0, 0, getZOffset() + 1);
            DropHandler.renderSlotProgressOverlay(matrices, slot);
            matrices.pop();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableDepthTest()V",
            ordinal = 1))
    public void dontdropit$renderDropTooltip(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!playerInventory.getCursorStack().isEmpty() && dontdropit$isClickOutsideBounds(mouseX, mouseY, x, y, 0)) {
            boolean blocked = false;
            if (!(((Object) this) instanceof CreativeInventoryScreen)) {
                blocked = !InputUtil.isKeyPressed(client.getWindow().getHandle(),
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
            List<Text> tooltipText = new ArrayList<>();
            if (blocked) {
                tooltipText.add(new TranslatableText("dontdropit.tooltip.drop.blocked").formatted(Formatting.BOLD, Formatting.RED));
                tooltipText.add(new TranslatableText("dontdropit.tooltip.drop.unblock_hint", DontDropItMod.keyForceDrop.getBoundKeyLocalizedText())
                                .formatted(Formatting.GRAY));
            } else
                tooltipText.add(new TranslatableText("dontdropit.tooltip.drop.allowed"));
            renderTooltip(matrices, tooltipText, mouseX, mouseY);
        }
    }

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;setZOffset(I)V",
            ordinal = 1))
    public void dontdropit$renderFavoriteIcon(MatrixStack matrices, Slot slot, CallbackInfo ci) {
        if (slot instanceof CreativeInventoryScreen.LockableSlot)
            return;
        RenderSystem.disableDepthTest();
        matrices.push();
        matrices.translate(0, 0, getZOffset());
        RenderSystem.enableBlend();
        DropHandler.renderSlotFavoriteIcon(matrices, slot);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        matrices.pop();
    }
}
