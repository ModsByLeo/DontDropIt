package adudecalledleo.dontdropit;

import adudecalledleo.dontdropit.config.ModConfig;
import adudecalledleo.dontdropit.util.FavoritesUtil;
import adudecalledleo.lionutils.ConfigHolder;
import adudecalledleo.lionutils.LoggerUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class DontDropItMod implements ClientModInitializer {
    public static final String MOD_ID = "dontdropit";
    public static final String MOD_NAME = "Don't Drop It!";

    public static Logger LOGGER = LoggerUtil.getLogger(MOD_NAME);
    public static final ConfigHolder<ModConfig> CONFIG_HOLDER = ConfigHolder.create(MOD_ID, ModConfig.class,
            ModConfig::new, ConfigHolder.createExceptionHandler(LOGGER));

    public static final KeyBinding keyDropStack = new KeyBinding("key.dontdropit.dropStack",
            GLFW.GLFW_KEY_LEFT_CONTROL, "key.categories.inventory");
    public static final KeyBinding keyForceDrop = new KeyBinding("key.dontdropit.forceDrop",
            GLFW.GLFW_KEY_LEFT_ALT, "key.categories.inventory");
    public static final KeyBinding keyToggleDropDelay = new KeyBinding("key.dontdropit.toggleDropDelay",
            GLFW.GLFW_KEY_UNKNOWN, "key.categories.inventory");

    @Override
    public void onInitializeClient() {
        FavoritesUtil.addConfigListener();
        CONFIG_HOLDER.loadConfig();
        KeyBindingHelper.registerKeyBinding(keyDropStack);
        KeyBindingHelper.registerKeyBinding(keyForceDrop);
        KeyBindingHelper.registerKeyBinding(keyToggleDropDelay);
        ClientTickEvents.END_CLIENT_TICK.register(DropHandler::onClientTick);
        LOGGER.info("Don't drop that Diamond Pickaxe!");
    }
}
