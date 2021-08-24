package adudecalledleo.dontdropit;

import adudecalledleo.dontdropit.api.DontDropItApi;
import adudecalledleo.dontdropit.config.DelayActivationMode;
import adudecalledleo.dontdropit.config.DropBehaviorOverride;
import adudecalledleo.dontdropit.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import java.util.Collections;
import java.util.Set;

public class DontDropIt implements ClientModInitializer, DontDropItApi {
    public static final String MOD_ID = "dontdropit";
    public static final String MOD_NAME = "Don't Drop It!";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitializeClient() {
        DropBehaviorOverride.registerConfigGuiProvider(ModConfig.class);
        DelayActivationMode.registerConfigGuiProvider(ModConfig.class);
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new)
                .registerSaveListener((manager, data) -> {
                    data.postSave();
                    return ActionResult.PASS;
                });
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
