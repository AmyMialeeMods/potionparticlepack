package xyz.amymialee.potionparticlepack;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PotionParticlePack implements ModInitializer {
    public static final String MOD_ID = "potionparticlepack";
    public static final Map<StatusEffect, Integer> effectColors = new HashMap<>();

    @Override // TODO: make particle effects render over entities like tf2 colorblind mode // also make them appear over heads and fade away like mincraft dungeons // render them like items like bake them a model like items/generated
    public void onInitialize() {}

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}