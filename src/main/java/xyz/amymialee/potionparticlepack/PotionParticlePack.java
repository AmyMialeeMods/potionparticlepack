package xyz.amymialee.potionparticlepack;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PotionParticlePack implements ModInitializer {
    public static final String MOD_ID = "potionparticlepack";
    public static final Map<StatusEffect, Integer> effectColors = new HashMap<>();
    public static final DefaultParticleType POTION_EFFECT = FabricParticleTypes.simple();

    @Override
    public void onInitialize() {
        Registry.register(Registries.PARTICLE_TYPE, id("potion_effect"), POTION_EFFECT);
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}