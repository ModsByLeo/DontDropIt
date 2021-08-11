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
        // this forces our key bindings to be updated in screens
        if (client.currentScreen != null && client.getWindow().getHandle() == window) {
            KeyBinding targetKeyBinding = null;
            for (KeyBinding keyBinding : ModKeyBindings.all) {
                if (keyBinding.matchesMouse(button)) {
                    targetKeyBinding = keyBinding;
                    break;
                }
            }
            if (targetKeyBinding == null)
                return;
            if (action == GLFW_RELEASE)
                targetKeyBinding.setPressed(false);
            else {
                targetKeyBinding.setPressed(true);
                ((KeyBindingAccessor) targetKeyBinding).setTimesPressed(
                        ((KeyBindingAccessor) targetKeyBinding).getTimesPressed() + 1);
            }
        }
    }
}
