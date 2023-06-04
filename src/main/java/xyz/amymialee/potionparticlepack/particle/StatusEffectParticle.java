package xyz.amymialee.potionparticlepack.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

public class StatusEffectParticle extends SpriteBillboardParticle {
    private static final Map<StatusEffect, ParticleTextureSheet> SHEETS = new HashMap<>();
    private final StatusEffect effect;

    protected StatusEffectParticle(ClientWorld clientWorld, StatusEffect effect, double d, double e, double f, double g, double h, double i) {
        super(clientWorld, d, e, f, 0, 0, 0);
        this.effect = effect;
        this.velocityMultiplier = 0.96F;
        this.velocityX = this.velocityX * 0.01F + g;
        this.velocityY = this.velocityY * 0.01F + h;
        this.velocityZ = this.velocityZ * 0.01F + i;
        this.x += (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
        this.y += (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
        this.z += (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
        this.maxAge = (int)(8.0 / (Math.random() * 0.8 + 0.2)) + 4;
    }

    @Override
    public ParticleTextureSheet getType() {
        if (SHEETS.containsKey(this.effect)) {
            return SHEETS.get(this.effect);
        }
        ParticleTextureSheet BLOCK_ATLAS_OPAQUE = new ParticleTextureSheet() {
            @Override
            public void begin(BufferBuilder builder, TextureManager textureManager) {
                RenderSystem.enableBlend();
                RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                RenderSystem.setShaderTexture(0, StatusEffectParticle.this.sprite.getAtlasId());
                builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
            }

            @Override
            public void draw(Tessellator tessellator) {
                tessellator.draw();
            }

            public String toString() {
                return "EFFECT_ATLAS_OPAQUE";
            }
        };
        SHEETS.put(this.effect, BLOCK_ATLAS_OPAQUE);
        return BLOCK_ATLAS_OPAQUE;
    }

    @Override
    public void move(double dx, double dy, double dz) {
        this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
        this.repositionFromBoundingBox();
    }

    @Override
    public float getSize(float tickDelta) {
        float f = ((float)this.age + tickDelta) / (float)this.maxAge;
        return this.scale * (1.0F - f * f * 0.5F);
    }

    @Override
    public int getBrightness(float tint) {
        float f = ((float)this.age + tint) / (float)this.maxAge;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        int i = super.getBrightness(tint);
        int j = i & 0xFF;
        int k = i >> 16 & 0xFF;
        j += (int)(f * 15.0F * 16.0F);
        if (j > 240) {
            j = 240;
        }

        return j | k << 16;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<StatusEffectParticleEffect> {
        @Override
        public Particle createParticle(StatusEffectParticleEffect defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            StatusEffect effect = Registries.STATUS_EFFECT.get(defaultParticleType.getStatusEffect());
            StatusEffectParticle effectParticle = new StatusEffectParticle(clientWorld, effect, d, e, f, g, h, i);
            StatusEffectSpriteManager statusEffectSpriteManager = MinecraftClient.getInstance().getStatusEffectSpriteManager();
            if (effect != null) {
                effectParticle.setSprite(statusEffectSpriteManager.getSprite(effect));
            }
            return effectParticle;
        }
    }
}