package adudecalledleo.dontdropit;

import adudecalledleo.lionutils.InitializerUtil;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    private ModKeyBindings() {
        InitializerUtil.initCtor();
    }

    public static final KeyBinding keyDropStack = new KeyBinding("key.dontdropit.dropStack",
            GLFW.GLFW_KEY_LEFT_CONTROL, "key.categories.dontdropit");
    public static final KeyBinding keyForceDrop = new KeyBinding("key.dontdropit.forceDrop",
            GLFW.GLFW_KEY_LEFT_ALT, "key.categories.dontdropit");
    public static final KeyBinding keyToggleDropDelay = new KeyBinding("key.dontdropit.toggleDropDelay",
            GLFW.GLFW_KEY_UNKNOWN, "key.categories.dontdropit");

    public static void register() {
        KeyBindingHelper.registerKeyBinding(keyDropStack);
        KeyBindingHelper.registerKeyBinding(keyForceDrop);
        KeyBindingHelper.registerKeyBinding(keyToggleDropDelay);
    }

    public static boolean isDown(KeyBinding keyBinding) {
        if (keyBinding.isUnbound())
            return false;
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(),
                KeyBindingHelper.getBoundKeyOf(keyBinding).getCode());
    }
}
