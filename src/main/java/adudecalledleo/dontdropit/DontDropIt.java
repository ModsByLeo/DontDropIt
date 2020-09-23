package adudecalledleo.dontdropit;

import adudecalledleo.dontdropit.config.ModConfig;
import adudecalledleo.lionutils.LoggerUtil;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class DontDropIt implements ClientModInitializer {
    public static final String MOD_ID = "dontdropit";
    public static final String MOD_NAME = "Don't Drop It!";

    public static final Logger LOGGER = LoggerUtil.getLogger(MOD_NAME);

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        LOGGER.info("Don't drop that Diamond Pickaxe!");
    }
}
