package adudecalledleo.dontdropit;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class DropAlertRenderer extends DrawableHelper {
    private static DropAlertRenderer instance;

    private DropAlertRenderer() { }

    public static void onHudRender(float tickDelta) {
        if (instance == null)
            instance = new DropAlertRenderer();
        instance.render(tickDelta);
    }

    public void render(float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer textRenderer = mc.textRenderer;
        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();

        if (mc.options.keyDrop.isPressed()) {
            ItemStack heldStack = mc.player.inventory.getMainHandStack();
            if (heldStack.isEmpty())
                return;
            int counter = DropKeyHandler.getTickCounter();
            String s = I18n.translate("dontdropit.alert",
                    (DropKeyHandler.isDroppingEntireStack() ? heldStack.getCount() : 1), heldStack.getName().getString());
            int sWidth = textRenderer.getStringWidth(s);
            int bX = (width - sWidth) / 2 - 2;
            int bW = MathHelper.floor((counter / (float)DropKeyHandler.getDropDelayTicks()) * (sWidth + 2));
            fill(bX + bW, height / 2 + textRenderer.fontHeight - 2,
                    bX + sWidth + 2, height / 2 + textRenderer.fontHeight * 2 + 2,
                    0xAF222222);
            fill(bX, height / 2 + textRenderer.fontHeight - 2,
                    bX + bW, height / 2 + textRenderer.fontHeight * 2 + 2,
                    0xAFAAAAAA);
            drawCenteredString(textRenderer, s, width / 2, height / 2 + textRenderer.fontHeight, 0xFFFFFF);
        }
    }

    public static void renderSlotDropProgress(Slot slot) {
        int counter = DropKeyHandler.getTickCounter();
        int sH = MathHelper.floor((counter / (float)DropKeyHandler.getDropDelayTicks()) * 16);
        fill(slot.xPosition, slot.yPosition + 16 - sH, slot.xPosition + 16, slot.yPosition + 16,
                0x8000FF00);
    }
}
