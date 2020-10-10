package adudecalledleo.dontdropit;

import adudecalledleo.dontdropit.config.FavoredChecker;
import adudecalledleo.dontdropit.config.ModConfig;
import adudecalledleo.lionutils.color.ColorUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class DropDelayRenderer {
    private static final Identifier TEX_FAVORITE = DontDropIt.id("textures/gui/favorite.png");

    public static void renderFavoriteIcon(MatrixStack matrices, ItemStack stack, int x, int y) {
        if (!ModConfig.get().favorites.drawOverlay || !FavoredChecker.isStackFavored(stack))
            return;
        MinecraftClient.getInstance().getTextureManager().bindTexture(TEX_FAVORITE);
        DrawableHelper.drawTexture(matrices, x, y, 18, 18, 18, 18, 18, 18);
    }

    private static final int COLOR_FORCE = ColorUtil.pack(0xFF, 0x00, 0x00, 0x30);
    private static final int COLOR_PROGRESS = ColorUtil.pack(0x00, 0xFF, 0x00, 0x30);

    public static void renderProgressOverlay(MatrixStack matrices, ItemStack stack, int x, int y, int w, int h) {
        if (!ModConfig.get().dropDelay.enabled)
            return;
        ItemStack currentStack = DropDelayHandler.getCurrentStack();
        if (currentStack.isEmpty() || currentStack != stack)
            return;
        if (!FavoredChecker.canDropStack(stack))
            return;
        if (stack.getCount() > 1 && DropDelayHandler.isDroppingEntireStack())
            DrawableHelper.fill(matrices, x, y, x + w, y + h, COLOR_FORCE);
        long counter = DropDelayHandler.getCounter();
        int progHeight = MathHelper.floor((counter / (double) DropDelayHandler.getCounterMax()) * h);
        DrawableHelper.fill(matrices, x, y + h - progHeight, x + w, y + h, COLOR_PROGRESS);
    }

    public static void renderOverlay(MatrixStack matrixStack, ItemStack stack, int x, int y, int z) {
        if (stack.isEmpty())
            return;
        matrixStack.push();
        matrixStack.translate(0, 0, z);
        RenderSystem.enableBlend();
        renderFavoriteIcon(matrixStack, stack, x - 1, y - 1);
        RenderSystem.disableBlend();
        matrixStack.translate(0, 0, 1);
        RenderSystem.colorMask(true, true, true, false);
        renderProgressOverlay(matrixStack, stack, x, y, 16, 16);
        RenderSystem.colorMask(true, true, true, true);
        matrixStack.pop();
    }
}
