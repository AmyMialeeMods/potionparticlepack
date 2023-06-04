package xyz.amymialee.potionparticlepack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;

public class PotionParticlePackClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new StatusEffectReloadListener());
        FabricLoader.getInstance().getModContainer(PotionParticlePack.MOD_ID).ifPresent(modContainer -> ResourceManagerHelper.registerBuiltinResourcePack(PotionParticlePack.id("legacy_colors"), modContainer, ResourcePackActivationType.NORMAL));
    }

    private static class StatusEffectReloadListener implements SimpleSynchronousResourceReloadListener {
        @Override
        public Identifier getFabricId() {
            return PotionParticlePack.id("effects");
        }

        @Override
        public void reload(ResourceManager manager) {
            manager.findAllResources("status_effects", path -> path.getPath().endsWith(".json")).forEach((identifier, resources) -> {
                for (Resource resource : resources) {
                    try (InputStream stream = resource.getInputStream()) {
                        JsonObject json = JsonParser.parseReader(new JsonReader(new InputStreamReader(stream))).getAsJsonObject();
                        Identifier effectId = new Identifier(identifier.getNamespace(), identifier.getPath().substring(15, identifier.getPath().length() - 5));
                        StatusEffect effect = Registries.STATUS_EFFECT.get(effectId);
                        if (effect != null) {
                            PotionParticlePack.effectColors.put(effect, json.get("color").getAsInt());
                        }
                    } catch (Exception ignored) {}
                }
            });
        }
    }
}