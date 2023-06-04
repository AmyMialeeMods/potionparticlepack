package xyz.amymialee.potionparticlepack.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.resource.SynchronousResourceReloader;
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
        if (!collection.isEmpty() && this.client.player != null) {
            RenderSystem.enableBlend();
            Sprite sprite = this.client.getStatusEffectSpriteManager().getSprite(collection.get((this.client.player.age / 20) % collection.size()).getEffectType());
            RenderSystem.setShaderTexture(0, sprite.getAtlasId());
            int size = 9;
            RenderSystem.setShaderColor(0.4f, 0.4f, 0.4f, 1.0f);
            DrawableHelper.drawSprite(matrices, x + 15 - size - 1, y + 16 - size - 1, 200, size, size, sprite);
            DrawableHelper.drawSprite(matrices, x + 16 - size - 1, y + 15 - size - 1, 200, size, size, sprite);
            DrawableHelper.drawSprite(matrices, x + 17 - size - 1, y + 16 - size - 1, 200, size, size, sprite);
            DrawableHelper.drawSprite(matrices, x + 16 - size - 1, y + 17 - size - 1, 200, size, size, sprite);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            DrawableHelper.drawSprite(matrices, x + 16 - size - 1, y + 16 - size - 1, 200, size, size, sprite);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}