package xyz.amymialee.potionparticlepack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import xyz.amymialee.potionparticlepack.cca.StatusComponent;

import java.io.InputStream;
import java.io.InputStreamReader;

public class PotionParticlePackClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new StatusEffectReloadListener());
        FabricLoader.getInstance().getModContainer(PotionParticlePack.MOD_ID).ifPresent(modContainer -> ResourceManagerHelper.registerBuiltinResourcePack(PotionParticlePack.id("new_colors"), modContainer, "§bNew Potion Colors", ResourcePackActivationType.NORMAL));
    }

    public static void renderParticles(LivingEntity entity, int baseColor) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == entity && MinecraftClient.getInstance().options.getPerspective() == Perspective.FIRST_PERSON) {
            return;
        }
        StatusComponent component = PotionParticlePackComponents.STATUS.get(entity);
        if (component.isActive() && baseColor >= 0) {
            boolean ambient = entity.getDataTracker().get(LivingEntity.POTION_SWIRLS_AMBIENT);
            boolean invisible = entity.isInvisible();
            float power = component.getWeight() / 4;
            if (invisible && entity.getRandom().nextInt(24) != 0) return;
            if (ambient && entity.getRandom().nextInt(3) != 0) return;
            while (power > 0) {
                if (power < 1) {
                    if (entity.getRandom().nextFloat() > power) {
                        break;
                    }
                }
                StatusEffect statusEffect = component.getRandomEffect();
                if (statusEffect != null) {
                    int color = statusEffect.getColor();
                    double d = (double) (color >> 16 & 0xFF) / 255.0;
                    double e = (double) (color >> 8 & 0xFF) / 255.0;
                    double f = (double) (color & 0xFF) / 255.0;
                    entity.getWorld().addParticle(ambient || invisible ? ParticleTypes.AMBIENT_ENTITY_EFFECT : ParticleTypes.ENTITY_EFFECT, entity.getParticleX(0.5), entity.getRandomBodyY(), entity.getParticleZ(0.5), d, e, f);
                }
                power--;
            }
        }
    }

    private static class StatusEffectReloadListener implements SimpleSynchronousResourceReloadListener {
        @Override
        public Identifier getFabricId() {
            return PotionParticlePack.id("effects");
        }

        @Override
        public void reload(ResourceManager manager) {
            PotionParticlePack.effectColors.clear();
            manager.findAllResources("status_effects", path -> path.getPath().endsWith(".json")).forEach((identifier, resources) -> {
                for (Resource resource : resources) {
                    try (InputStream stream = resource.getInputStream()) {
                        JsonObject json = JsonParser.parseReader(new JsonReader(new InputStreamReader(stream))).getAsJsonObject();
                        Identifier effectId = new Identifier(identifier.getNamespace(), identifier.getPath().substring(15, identifier.getPath().length() - 5));
                        StatusEffect effect = Registry.STATUS_EFFECT.get(effectId);
                        if (effect != null) {
                            PotionParticlePack.effectColors.put(effect, json.get("color").getAsInt());
                        }
                    } catch (Exception ignored) {}
                }
            });
        }
    }
}