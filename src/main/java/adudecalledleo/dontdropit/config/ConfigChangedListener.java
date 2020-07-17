package adudecalledleo.dontdropit.config;

@FunctionalInterface
public interface ConfigChangedListener {
    void onConfigChanged(ModConfig config);
}
