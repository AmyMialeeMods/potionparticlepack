package xyz.amymialee.potionparticlepack.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.block.Blocks;
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
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.amymialee.potionparticlepack.PotionParticlePack;
import xyz.amymialee.potionparticlepack.PotionParticlePackComponents;
import xyz.amymialee.potionparticlepack.cca.StatusComponent;
import xyz.amymialee.potionparticlepack.particle.StatusEffectParticleEffect;

import java.util.Collection;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow @Final private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;
    @Shadow @Final private static TrackedData<Boolean> POTION_SWIRLS_AMBIENT;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z", at = @At("RETURN"))
    private void potionParticlePack$addStatusParticle(StatusEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            if (this.world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(new StatusEffectParticleEffect(PotionParticlePack.POTION_EFFECT, effect.getEffectType()),
                        this.getX(), this.getY() + this.getHeight(), this.getZ(), 10, 1, 1, 1, 0.01);
            }
        }
    }

    @WrapOperation(method = "tickStatusEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;get(Lnet/minecraft/entity/data/TrackedData;)Ljava/lang/Object;", ordinal = 0))
    private Object potionParticlePack$hideVanillaParticles(DataTracker tracker, TrackedData<Object> data, Operation<Object> operation, @Share("color") LocalIntRef color) {
        if (tracker.get(data) instanceof Integer integer) {
            color.set(integer);
        } else {
            color.set(-1);
        }
        return 0;
    }

    @Inject(method = "tickStatusEffects", at = @At("TAIL"))
    private void potionParticlePack$showParticles(CallbackInfo ci, @Share("color") LocalIntRef baseColor) {
        if (!this.world.isClient) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        LivingEntity thisEntity = (LivingEntity) (Object) this;
        if (player == thisEntity && MinecraftClient.getInstance().options.getPerspective() == Perspective.FIRST_PERSON) {
            return;
        }
        int potionColor = baseColor.get();
        if (potionColor >= 0) {
            StatusComponent component = PotionParticlePackComponents.STATUS.get(this);
            boolean ambient = this.dataTracker.get(POTION_SWIRLS_AMBIENT);
            boolean invisible = this.isInvisible();
            boolean shouldParticle = this.random.nextFloat() > 0.3f && (!invisible || this.random.nextInt(5) == 0) && (!ambient || this.random.nextInt(3) == 0);
            if (shouldParticle) {
                StatusEffect statusEffect = component.getRandomEffect();
                if (statusEffect != null) {
                    int color = statusEffect.getColor();
                    double d = (double) (color >> 16 & 0xFF) / 255.0;
                    double e = (double) (color >> 8 & 0xFF) / 255.0;
                    double f = (double) (color & 0xFF) / 255.0;
                    this.getWorld().addParticle(ambient ? ParticleTypes.AMBIENT_ENTITY_EFFECT : ParticleTypes.ENTITY_EFFECT, this.getParticleX(0.5), this.getRandomBodyY(), this.getParticleZ(0.5), d, e, f);
                }
            }
        }
    }

    @Inject(method = "clearPotionSwirls", at = @At(value = "TAIL"))
    private void potionParticlePack$clearParticles(CallbackInfo ci) {
        PotionParticlePackComponents.STATUS.get(this).clear();
    }

    @Inject(method = "updatePotionVisibility", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionUtil;getColor(Ljava/util/Collection;)I"))
    private void getColor(CallbackInfo ci) {
        StatusComponent component = PotionParticlePackComponents.STATUS.get(this);
        Collection<StatusEffectInstance> collection = this.activeStatusEffects.values();
        component.setEffects(collection);
    }
}