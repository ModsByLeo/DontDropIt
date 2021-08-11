package adudecalledleo.dontdropit.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow @Final public GameOptions options;

    @Unique
    private boolean disableDropKey(KeyBinding binding) {
        if (binding == options.keyDrop)
            return false;
        return binding.wasPressed();
    }

    // in 1.16.4 and above, this redirects the keySwapHands check
    @Redirect(method = "handleInputEvents",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/options/KeyBinding;wasPressed()Z",
                       ordinal = 6))
    public boolean disableDropKey1(KeyBinding binding) {
        return disableDropKey(binding);
    }

    // this one's for 1.16.4, since it added an additional keybind (social interactions) that shifted the drop key by 1
    // in 1.16.3 and below, this redirects the keyChat check
    @Redirect(method = "handleInputEvents",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/options/KeyBinding;wasPressed()Z",
                       ordinal = 7))
    public boolean disableDropKey2(KeyBinding binding) {
        return disableDropKey(binding);
    }
}
