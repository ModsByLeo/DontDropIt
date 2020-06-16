package adudecalledleo.dontdropit;

import adudecalledleo.dontdropit.config.ModConfigHolder;
import net.fabricmc.api.ClientModInitializer;
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
        ModConfigHolder.loadConfig();
        ClientTickCallback.EVENT.register(DropHandler::onClientTick);
        log(Level.INFO, "Don't drop the diamond pickaxe!");
    }

    public static void log(Level level, String message){
        LOGGER.log(level, message);
    }
}
