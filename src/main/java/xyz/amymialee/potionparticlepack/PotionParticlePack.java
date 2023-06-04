package xyz.amymialee.potionparticlepack;

import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xyz.amymialee.potionparticlepack.particle.StatusEffectParticleEffect;

import java.util.HashMap;
import java.util.Map;

public class PotionParticlePack implements ModInitializer {
    public static final String MOD_ID = "potionparticlepack";
    public static final Map<StatusEffect, Integer> effectColors = new HashMap<>();
    public static final ParticleType<StatusEffectParticleEffect> POTION_EFFECT = FabricParticleTypes.complex(StatusEffectParticleEffect.PARAMETERS_FACTORY);

    @Override
    public void onInitialize() {
        Registry.register(Registries.PARTICLE_TYPE, id("potion_effect"), new ParticleType<>(true, StatusEffectParticleEffect.PARAMETERS_FACTORY) {
            @Override
            public Codec<StatusEffectParticleEffect> getCodec() {
                return StatusEffectParticleEffect.createCodec(this);
            }
        });
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}