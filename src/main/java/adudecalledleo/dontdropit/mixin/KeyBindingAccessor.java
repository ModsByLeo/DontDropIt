package adudecalledleo.dontdropit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.option.KeyBinding;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
    @Accessor int getTimesPressed();
    @Accessor void setTimesPressed(int timesPressed);
}
