package adudecalledleo.dontdropit.config;

import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;

public class ModConfigSerializer extends JanksonConfigSerializer<ModConfig> {
    public ModConfigSerializer(Config definition) {
        super(definition, ModConfig.class);
    }

    @Override
    public void serialize(ModConfig config) throws SerializationException {
        config.favorites.postLoad();
        FavoredChecker.updateFavoredSets(config);
        super.serialize(config);
    }

    @Override
    public ModConfig createDefault() {
        return new ModConfig();
    }
}
