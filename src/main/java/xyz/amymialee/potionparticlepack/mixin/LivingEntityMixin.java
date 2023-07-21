package xyz.amymialee.potionparticlepack.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.amymialee.potionparticlepack.PotionParticlePackClient;
import xyz.amymialee.potionparticlepack.PotionParticlePackComponents;
import xyz.amymialee.potionparticlepack.cca.StatusComponent;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow @Final private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @WrapOperation(method = "tickStatusEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;get(Lnet/minecraft/entity/data/TrackedData;)Ljava/lang/Object;", ordinal = 0))
    private Object potionParticlePack$hideVanillaParticles(DataTracker tracker, TrackedData<Object> data, Operation<Object> operation, @Share("color") LocalIntRef color) {
        if (PotionParticlePackComponents.STATUS.get(this).isActive()) {
            if (tracker.get(data) instanceof Integer integer) {
                color.set(integer);
            } else {
                color.set(-1);
            }
            return 0;
        }
        return operation.call(tracker, data);
    }

    @Inject(method = "tickStatusEffects", at = @At("TAIL"))
    private void potionParticlePack$showParticles(CallbackInfo ci, @Share("color") LocalIntRef baseColor) {
        if (!this.world.isClient) return;
        LivingEntity thisEntity = (LivingEntity) (Object) this;
        PotionParticlePackClient.renderParticles(thisEntity, baseColor.get());
    }

    @Inject(method = "clearPotionSwirls", at = @At(value = "TAIL"))
    private void potionParticlePack$clearParticles(CallbackInfo ci) {
        PotionParticlePackComponents.STATUS.get(this).clear();
    }

    @Inject(method = "updatePotionVisibility", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionUtil;getColor(Ljava/util/Collection;)I"))
    private void getColor(CallbackInfo ci) {
        PotionParticlePackComponents.STATUS.get(this).setEffects(this.activeStatusEffects.values());
    }
}