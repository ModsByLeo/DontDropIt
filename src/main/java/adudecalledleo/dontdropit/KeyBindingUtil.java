package adudecalledleo.dontdropit;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

public final class KeyBindingUtil {
    private KeyBindingUtil() {
        throw new UnsupportedOperationException("ha, no");
    }

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    public static boolean isDown(MinecraftClient client, KeyBinding keyBinding) {
        InputUtil.Key key = KeyBindingHelper.getBoundKeyOf(keyBinding);
        return !key.equals(InputUtil.UNKNOWN_KEY) && InputUtil.isKeyPressed(client.getWindow().getHandle(), key.getCode());
    }

    public static boolean isDown(KeyBinding keyBinding) {
        return isDown(CLIENT, keyBinding);
    }
}
