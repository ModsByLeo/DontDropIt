package adudecalledleo.dontdropit.mixin;

import adudecalledleo.dontdropit.DropDelayRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {
    @Unique private final MatrixStack matrixStack = new MatrixStack();

    @Inject(method = "renderHotbarItem", at = @At(value = "TAIL"))
    public void renderHotbarDropProgress(int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        DropDelayRenderer.renderOverlay(matrixStack, stack, x, y, getZOffset());
    }
}
