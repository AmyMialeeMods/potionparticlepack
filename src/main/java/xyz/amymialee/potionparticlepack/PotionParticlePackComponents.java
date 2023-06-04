package xyz.amymialee.potionparticlepack;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.minecraft.entity.LivingEntity;
import xyz.amymialee.potionparticlepack.cca.StatusComponent;

public class PotionParticlePackComponents implements EntityComponentInitializer {
    public static final ComponentKey<StatusComponent> STATUS = ComponentRegistry.getOrCreate(PotionParticlePack.id("status"), StatusComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(LivingEntity.class, STATUS).end(StatusComponent::new);
    }
}