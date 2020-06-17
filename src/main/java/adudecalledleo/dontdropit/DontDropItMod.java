package adudecalledleo.dontdropit;

import adudecalledleo.dontdropit.config.ModConfigHolder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.options.KeyBinding;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class DontDropItMod implements ClientModInitializer {
    public static final String MOD_ID = "dontdropit";
    public static final String MOD_NAME = "Don't Drop It!";

    public static Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static KeyBinding keyFavoriteOverride = new KeyBinding("key.dontdropit.favoriteOverride",
            GLFW.GLFW_KEY_LEFT_ALT, "key.categories.inventory");

    @Override
    public void onInitializeClient() {
        ModConfigHolder.loadConfig();
        KeyBindingHelper.registerKeyBinding(keyFavoriteOverride);
        ClientTickCallback.EVENT.register(DropHandler::onClientTick);
        log(Level.INFO, "Don't drop the diamond pickaxe!");
    }

    public static void log(Level level, String message){
        LOGGER.log(level, message);
    }
}
