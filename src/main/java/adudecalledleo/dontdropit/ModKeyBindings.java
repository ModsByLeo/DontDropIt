package adudecalledleo.dontdropit;

import adudecalledleo.lionutils.InitializerUtil;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;

import static org.lwjgl.glfw.GLFW.*;

public class ModKeyBindings {
    private ModKeyBindings() {
        InitializerUtil.initCtor();
    }

    public static final KeyBinding keyDropStack = new KeyBinding("key.dontdropit.dropStack",
            GLFW_KEY_LEFT_CONTROL, "key.categories.dontdropit");
    public static final KeyBinding keyForceDrop = new KeyBinding("key.dontdropit.forceDrop",
            GLFW_KEY_LEFT_ALT, "key.categories.dontdropit");
    public static final KeyBinding keyToggleDropDelay = new KeyBinding("key.dontdropit.toggleDropDelay",
            GLFW_KEY_UNKNOWN, "key.categories.dontdropit");

    public static final KeyBinding[] all = new KeyBinding[] { keyDropStack, keyForceDrop, keyToggleDropDelay };

    public static void register() {
        for (KeyBinding keyBinding : all)
            KeyBindingHelper.registerKeyBinding(keyBinding);
    }

    public static boolean isDown(KeyBinding keyBinding) {
        if (keyBinding.isUnbound())
            return false;
        return keyBinding.isPressed();
    }
}
