package xyz.amymialee.potionparticlepack.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin implements SynchronousResourceReloader {
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private MatrixStack matrices;

    @Inject(method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V", at = @At("HEAD"), cancellable = true)
    private void potionParticlePack$hideItem(LivingEntity entity, World world, ItemStack stack, int x, int y, int seed, int z, CallbackInfo ci) {
        if (Screen.hasShiftDown() && this.client.currentScreen != null) {
            if (!PotionUtil.getPotionEffects(stack).isEmpty()) {
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.4f);
            }
        }
    }

    @Inject(method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V", at = @At(value = "TAIL"))
    private void potionParticlePack$letsSeeTheEffects(LivingEntity entity, World world, ItemStack stack, int x, int y, int seed, int z, CallbackInfo ci) {
        var collection = PotionUtil.getPotionEffects(stack);
        if (this.client.player != null && Screen.hasShiftDown() && this.client.currentScreen != null) {
            for (var i = 0; i < collection.size(); i++) {
                var instance = collection.get(i);
                RenderSystem.enableBlend();
                var sprite = this.client.getStatusEffectSpriteManager().getSprite(instance.getEffectType());
                RenderSystem.setShaderTexture(0, sprite.getAtlasId());
                var size = 18 / collection.size();
                RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                var xOffset = -1 + (18 / collection.size() * (i));
                var yOffset = -1 + (18 - size) / 2;
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                this.renderIcon(this.matrices, sprite, x, y, size, xOffset, yOffset);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }

    @Unique
    private void renderIcon(@NotNull MatrixStack matrices, @NotNull Sprite sprite, int x, int y, int size, float xOffset, float yOffset) {
        var matrix = matrices.peek().getPositionMatrix();
        var bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, x + xOffset, y + yOffset, 200).texture(sprite.getMinU(), sprite.getMinV()).next();
        bufferBuilder.vertex(matrix, x + xOffset, y + yOffset + size, 200).texture(sprite.getMinU(), sprite.getMaxV()).next();
        bufferBuilder.vertex(matrix, x + xOffset + size, y + yOffset + size, 200).texture(sprite.getMaxU(), sprite.getMaxV()).next();
        bufferBuilder.vertex(matrix, x + xOffset + size, y + yOffset, 200).texture(sprite.getMaxU(), sprite.getMinV()).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }
}