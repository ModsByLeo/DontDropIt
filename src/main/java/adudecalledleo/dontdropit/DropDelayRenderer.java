package adudecalledleo.dontdropit;

import adudecalledleo.dontdropit.config.FavoredChecker;
import adudecalledleo.dontdropit.config.ModConfig;
import com.mojang.blaze3d.systems.RenderSystem;

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
        RenderSystem.setShaderTexture(0, TEX_FAVORITE);
        DrawableHelper.drawTexture(matrices, x, y, 18, 18, 18, 18, 18, 18);
    }

    private static int pack(int r, int g, int b, int a) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    private static final int COLOR_FORCE = pack(0xFF, 0x00, 0x00, 0x30);
    private static final int COLOR_PROGRESS = pack(0x00, 0xFF, 0x00, 0x30);

    public static void renderProgressOverlay(MatrixStack matrices, ItemStack stack, int x, int y, int w, int h) {
        ItemStack currentStack = DropDelayHandler.getCurrentStack();
        if (currentStack.isEmpty() || currentStack != stack)
            return;
        if (!ModConfig.get().dropDelay.mode.isEnabled(stack))
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
        RenderSystem.enableBlend();
        matrixStack.push();
        matrixStack.translate(0, 0, z + 200);
        renderFavoriteIcon(matrixStack, stack, x - 1, y - 1);
        matrixStack.translate(0, 0, 5);
        RenderSystem.colorMask(true, true, true, false);
        renderProgressOverlay(matrixStack, stack, x, y, 16, 16);
        RenderSystem.colorMask(true, true, true, true);
        matrixStack.pop();
        RenderSystem.disableBlend();
    }
}
