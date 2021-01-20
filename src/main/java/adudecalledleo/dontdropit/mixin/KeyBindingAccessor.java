package adudecalledleo.dontdropit.mixin;

import net.minecraft.client.options.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
    @Accessor int getTimesPressed();
    @Accessor void setTimesPressed(int timesPressed);
}
