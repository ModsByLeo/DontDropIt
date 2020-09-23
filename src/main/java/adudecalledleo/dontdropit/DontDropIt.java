package adudecalledleo.dontdropit;

import adudecalledleo.dontdropit.config.ModConfig;
import adudecalledleo.dontdropit.config.ModConfigSerializer;
import adudecalledleo.lionutils.LoggerUtil;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;

public class DontDropIt implements ClientModInitializer {
    public static final String MOD_ID = "dontdropit";
    public static final String MOD_NAME = "Don't Drop It!";

    public static final Logger LOGGER = LoggerUtil.getLogger(MOD_NAME);

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, (definition, configClass) -> new ModConfigSerializer(definition));
        ModKeyBindings.register();
        ClientTickEvents.END_CLIENT_TICK.register(DropDelayHandler::tick);
        LOGGER.info("Don't drop that Diamond Pickaxe!");
    }
}
