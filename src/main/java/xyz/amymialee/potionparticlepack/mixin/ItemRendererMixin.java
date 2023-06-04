package xyz.amymialee.potionparticlepack.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.resource.SynchronousResourceReloader;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin implements SynchronousResourceReloader {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
    private void potionParticlePack$letsSeeTheEffects(MatrixStack matrices, TextRenderer textRenderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
        List<StatusEffectInstance> collection = PotionUtil.getPotionEffects(stack);
        if (this.client.player != null) {
            for (StatusEffectInstance instance : collection) {
                RenderSystem.enableBlend();
                Sprite sprite = this.client.getStatusEffectSpriteManager().getSprite(collection.get((this.client.player.age / 20) % collection.size()).getEffectType());
                RenderSystem.setShaderTexture(0, sprite.getAtlasId());
                int size = 9;
                RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                Matrix4f matrix = matrices.peek().getPositionMatrix();
                float xOffset = -1; // + -MathHelper.sin((this.client.player.age + this.client.getTickDelta()) * (float) (Math.PI / 180.0) * 4) * 6;
                float yOffset = -1; // + MathHelper.cos((this.client.player.age + this.client.getTickDelta()) * (float) (Math.PI / 180.0) * 4) * 6;
                BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
                bufferBuilder.vertex(matrix, x + xOffset, y + yOffset, 200).texture(sprite.getMinU(), sprite.getMinV()).next();
                bufferBuilder.vertex(matrix, x + xOffset, y + yOffset + size, 200).texture(sprite.getMinU(), sprite.getMaxV()).next();
                bufferBuilder.vertex(matrix, x + xOffset + size, y + yOffset + size, 200).texture(sprite.getMaxU(), sprite.getMaxV()).next();
                bufferBuilder.vertex(matrix, x + xOffset + size, y + yOffset, 200).texture(sprite.getMaxU(), sprite.getMinV()).next();
                BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }
}