package xyz.amymialee.potionparticlepack.mixin;

import net.minecraft.entity.effect.StatusEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.amymialee.potionparticlepack.PotionParticlePack;

@Mixin(StatusEffect.class)
public class StatusEffectMixin {
    @Inject(method = "getColor", at = @At("HEAD"), cancellable = true)
    private void potionParticlePack$newColor(CallbackInfoReturnable<Integer> cir) {
        StatusEffect effect = (StatusEffect) (Object) this;
        if (PotionParticlePack.effectColors.containsKey(effect)) {
            cir.setReturnValue(PotionParticlePack.effectColors.get(effect));
        }
    }
}