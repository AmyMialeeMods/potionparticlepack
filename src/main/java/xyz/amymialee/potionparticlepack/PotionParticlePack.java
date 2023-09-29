package xyz.amymialee.potionparticlepack;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PotionParticlePack implements ModInitializer {
    public static final String MOD_ID = "potionparticlepack";
    public static final Map<StatusEffect, Integer> effectColors = new HashMap<>();

    @Override
    public void onInitialize() {
        MidnightConfig.init(MOD_ID, PotionParticlePackConfig.class);
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}