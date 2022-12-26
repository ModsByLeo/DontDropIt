package adudecalledleo.dontdropit.mixin;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import adudecalledleo.dontdropit.ModKeyBindings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.KeyBinding;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow private double x;
    @Shadow private double y;

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    public void updateModKeys(long window, int button, int action, int mods, CallbackInfo ci) {
        // this forces our key bindings (and the drop key binding) to be updated in handled screens
        // not required for mouse bindings, but is much more convenient than handling them manually
        if (client.getWindow().getHandle() == window && client.currentScreen instanceof HandledScreen<?> screen) {
            Element hoveredElement = screen.hoveredElement(x, y).orElse(null);
            if (hoveredElement instanceof ClickableWidget) {
                // mouse is over something clickable, don't update keys!
                return;
            }

            KeyBinding targetBinding = null;
            if (client.options.dropKey.matchesMouse(button))
                targetBinding = client.options.dropKey;
            else {
                for (KeyBinding keyBinding : ModKeyBindings.all) {
                    if (keyBinding.matchesMouse(button)) {
                        targetBinding = keyBinding;
                        break;
                    }
                }
            }

            if (targetBinding == null)
                return;
            if (action == GLFW_RELEASE)
                targetBinding.setPressed(false);
            else {
                targetBinding.setPressed(true);
                ((KeyBindingAccessor) targetBinding).setTimesPressed(((KeyBindingAccessor) targetBinding).getTimesPressed() + 1);
            }
        }
    }
}
