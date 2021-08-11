package adudecalledleo.dontdropit.mixin;

import adudecalledleo.dontdropit.ModKeyBindings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    public void updateModKeys(long window, int button, int action, int mods, CallbackInfo ci) {
        // this forces our key bindings (and the drop key binding) to be updated in screens
        if (client.currentScreen != null && client.getWindow().getHandle() == window) {
            KeyBinding targetBinding = null;
            if (client.options.keyDrop.matchesMouse(button))
                targetBinding = client.options.keyDrop;
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
