package adudecalledleo.dontdropit;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DontDropItMod implements ClientModInitializer {
    public static final String MOD_ID = "dontdropit";
    public static final String MOD_NAME = "Don't Drop It!";

    public static Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @Override
    public void onInitializeClient() {
        log(Level.INFO, "Initializing client");
        ClientTickCallback.EVENT.register(DropKeyHandler::onClientTick);
        HudRenderCallback.EVENT.register(DropAlertRenderer::onHudRender);
    }

    public static void log(Level level, String message){
        LOGGER.log(level, message);
    }
}
