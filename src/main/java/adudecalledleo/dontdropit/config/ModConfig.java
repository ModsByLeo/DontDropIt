package adudecalledleo.dontdropit.config;

public class ModConfig {
    public ModConfig() {
        dropDelay = new DropDelay();
        dropBlock = new DropBlock();
    }

    public static class DropDelay {
        public boolean enabled = true;
        public int ticks = 10;
    }

    public DropDelay dropDelay;

    public static class DropBlock {
        public boolean enabled = true;
        public boolean enchanted = true;
        public boolean cursed = false;
    }

    public DropBlock dropBlock;
}
