package adudecalledleo.dontdropit;

import adudecalledleo.dontdropit.api.DontDropItApi;
import adudecalledleo.dontdropit.config.ModConfig;
import adudecalledleo.dontdropit.config.ModConfigSerializer;
import adudecalledleo.lionutils.LoggerUtil;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Set;

public class DontDropIt implements ClientModInitializer, DontDropItApi {
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
        IgnoredSlots.collectFromEntrypoints();
        ClientTickEvents.END_CLIENT_TICK.register(DropDelayHandler::tick);
        LOGGER.info("Don't drop that Diamond Pickaxe!");
    }

    @Override
    public Set<Class<? extends Slot>> getIgnoredDropDelaySlots() {
        return Collections.singleton(CreativeInventoryScreen.LockableSlot.class);
    }
}
