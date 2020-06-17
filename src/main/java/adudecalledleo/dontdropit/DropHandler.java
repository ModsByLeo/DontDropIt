package adudecalledleo.dontdropit;

import adudecalledleo.dontdropit.api.HandledScreenDropHandlerInterface;
import adudecalledleo.dontdropit.api.HandledScreenExtensions;
import adudecalledleo.dontdropit.api.DefaultDropHandlerInterface;
import adudecalledleo.dontdropit.api.DropHandlerInterface;
import adudecalledleo.dontdropit.config.ModConfigHolder;
import adudecalledleo.dontdropit.util.ConfigUtil;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class DropHandler {
    private static DropHandler instance;

    private DropHandler() { }

    public static void onClientTick(MinecraftClient mc, DropHandlerInterface dhi) {
        if (instance == null)
            instance = new DropHandler();
        instance.tick(mc, dhi);
    }

    private static HandledScreenDropHandlerInterface csdhi;

    public static void onClientTick(MinecraftClient mc) {
        if (mc.currentScreen == null)
            onClientTick(mc, DefaultDropHandlerInterface.INSTANCE);
        else if (mc.currentScreen instanceof HandledScreenExtensions) {
            if (csdhi == null || csdhi.getHse() != mc.currentScreen)
                csdhi = new HandledScreenDropHandlerInterface((HandledScreenExtensions) mc.currentScreen);
            onClientTick(mc, csdhi);
        }
    }

    public static int getDropDelayTicks() {
        return ModConfigHolder.getConfig().dropDelay.ticks;
    }

    public static int getTickCounter() {
        if (instance == null)
            return 0;
        return instance.dropDelayCounter;
    }

    public static boolean isDroppingEntireStack() {
        if (instance == null)
            return false;
        return instance.controlWasDown;
    }

    private int dropDelayCounter = 0;
    private ItemStack currentStack = ItemStack.EMPTY;
    private boolean controlWasDown = false;

    public void tick(MinecraftClient mc, DropHandlerInterface dhi) {
        if (ModConfigHolder.getConfig().dropDelay.enabled) {
            if (dhi.isKeyDown(mc.options.keyDrop, mc)) {
                if (dropDelayCounter < getDropDelayTicks()) {
                    ItemStack stack = dhi.getCurrentStack(mc);
                    if (dropDelayCounter == 0)
                        controlWasDown = Screen.hasControlDown() && stack.getCount() > 1;
                    else if (controlWasDown != Screen.hasControlDown() && stack.getCount() > 1) {
                            dropDelayCounter = 0;
                            return;
                        }
                    if (stack.isEmpty() || !dhi.canDropStack(stack, mc) || (dropDelayCounter > 0 && stack != currentStack)) {
                        dropDelayCounter = 0;
                        return;
                    }
                    currentStack = stack;
                    dropDelayCounter++;
                } else {
                    dropDelayCounter = 0;
                    dhi.drop(controlWasDown, mc);
                }
            } else
                dropDelayCounter = 0;
        } else {
            // 1. use the KeyBinding directly so we don't drop twice in ContainerScreens (KeyBindings aren't updated in Screens)
            // 2. use wasPressed() instead of isPressed() so we only drop an item once per tap
            if (mc.options.keyDrop.wasPressed()) {
                ItemStack stack = dhi.getCurrentStack(mc);
                if (dhi.canDropStack(stack, mc))
                    dhi.drop(Screen.hasControlDown(), mc);
            }
        }
    }

    private static final Identifier TEX_FAVORITE = new Identifier(DontDropItMod.MOD_ID, "textures/gui/favorite.png");

    public static void renderSlotFavoriteIcon(MatrixStack matrices, ItemStack stack,
                                              int x, int y) {
        if (ConfigUtil.isStackFavorite(stack)) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(TEX_FAVORITE);
            DrawableHelper.drawTexture(matrices, x, y, 16, 16, 16, 16, 16, 16);
        }
    }

    public static void renderSlotFavoriteIcon(MatrixStack matrices, Slot slot) {
        renderSlotFavoriteIcon(matrices, slot.getStack(), slot.x, slot.y);
    }

    public static void renderSlotProgressOverlay(MatrixStack matrices, ItemStack stack,
                                                 int x, int y, int w, int h) {
        if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(),
                KeyBindingHelper.getBoundKeyOf(DontDropItMod.keyForceDrop).getCode())
          && ConfigUtil.isStackFavorite(stack))
            return;
        if (stack.getCount() > 1 && isDroppingEntireStack())
            DrawableHelper.fill(matrices, x, y, x + w, y + h, 0x20FF0000);
        int counter = getTickCounter();
        int sH = MathHelper.floor((counter / (float) getDropDelayTicks()) * h);
        DrawableHelper.fill(matrices, x, y + h - sH, x + w, y + h, 0x4000FF00);
    }

    public static void renderSlotProgressOverlay(MatrixStack matrices, Slot slot) {
        renderSlotProgressOverlay(matrices, slot.getStack(), slot.x, slot.y, 16, 16);
    }
}
