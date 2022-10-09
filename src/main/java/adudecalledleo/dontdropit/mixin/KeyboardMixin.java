package adudecalledleo.dontdropit.mixin;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import adudecalledleo.dontdropit.ModKeyBindings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onKey", at = @At(value = "HEAD"))
    public void updateModKeys(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
        // this forces our key bindings (and the drop key binding) to be updated in handled screens
        // this allows scancodes to work properly, since you can't poll them via GLFW
        if (client.getWindow().getHandle() == window && client.currentScreen instanceof HandledScreen<?> screen) {
            if (screen.getFocused() instanceof TextFieldWidget textFieldWidget) {
                if (textFieldWidget.isActive()) {
                    // a text field widget is active, don't update keys!
                    return;
                }
            }

            KeyBinding targetBinding = null;
            if (client.options.dropKey.matchesKey(key, scancode))
                targetBinding = client.options.dropKey;
            else {
                for (KeyBinding keyBinding : ModKeyBindings.all) {
                    if (keyBinding.matchesKey(key, scancode)) {
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
