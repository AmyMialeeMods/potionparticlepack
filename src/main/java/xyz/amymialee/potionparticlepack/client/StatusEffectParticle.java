package xyz.amymialee.potionparticlepack.client;

import com.luxintrus.befoul.client.model.MothModel;
import com.luxintrus.befoul.core.BefoulBlocks;
import com.luxintrus.befoul.core.BefoulMod;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;

public class StatusEffectParticle extends Particle {

    private final MothModel model;
    private final VertexConsumerProvider.Immediate immediate;
    private final Identifier texture;

    private final int timeOffset;
    private final float orbitSpeed;
    private final float rollSpeed;
    private final float distance;
    private final boolean spawnedInLumenLightOrMothSource;
    private boolean markedForRemoval;
    private final BlockPos pos;

    private static final Identifier MOTH_TEXTURE = BefoulMod.id("textures/particle/moth.png");
    private static final Identifier LUNAR_MOTH_TEXTURE = BefoulMod.id("textures/particle/lunar_moth.png");

    public StatusEffectParticle(ClientWorld world, double x, double y, double z, MothModel model, Identifier texture) {
        super(world, x, y, z);
        this.model = model;
        this.immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        this.texture = texture;
        this.gravityStrength = 0;
        this.maxAge = this.random.rangeClosed(800, 1200);
        this.timeOffset = this.random.nextInt(10000);
        this.orbitSpeed = 6 + 4 * this.random.nextFloat();
        this.rollSpeed = 0.05f + 0.3f * this.random.nextFloat();
        this.distance = 1f + 0.6f * this.random.nextFloat();
        this.pos = new BlockPos(x, y, z);
        this.spawnedInLumenLightOrMothSource = world.getBlockState(this.pos).isOf(BefoulBlocks.LUMEN_LIGHT) || world.getBlockState(this.pos).isOf(BefoulBlocks.MOTH_SOURCE);
        this.markedForRemoval = false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.spawnedInLumenLightOrMothSource && !this.markedForRemoval
                && !(this.world.getBlockState(this.pos).isOf(BefoulBlocks.LUMEN_LIGHT) || this.world.getBlockState(this.pos).isOf(BefoulBlocks.MOTH_SOURCE))) {
            this.markForRemoval();
        } else if (!this.markedForRemoval && !this.world.getFluidState(this.pos).isEmpty()) {
            this.markForRemoval();
        }
    }

    private void markForRemoval() {
        this.markedForRemoval = true;
        this.maxAge = Math.min(this.maxAge, this.age + 100);
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Vec3d pos = camera.getPos();
        float lerpedX = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - pos.getX());
        float lerpedY = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - pos.getY() - 1.5);
        float lerpedZ = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - pos.getZ());

        MatrixStack stack = new MatrixStack();
        stack.translate(lerpedX, lerpedY, lerpedZ);

        float time = this.timeOffset + this.age + tickDelta;
        float roll = (float) (Math.sin(time * this.rollSpeed) * 45 * MathHelper.RADIANS_PER_DEGREE);
        float yaw = time * this.orbitSpeed * MathHelper.RADIANS_PER_DEGREE;

        Vec3d offset = new Vec3d(this.distance, 0, 0)
                .rotateZ(roll)
                .rotateY(yaw);
        stack.translate(offset.x, offset.y, offset.z);

        this.angle = this.random.rangeClosed(-50, 50) * MathHelper.RADIANS_PER_DEGREE;
        this.model.root.scaleX = 0.5f;
        this.model.root.scaleY = 0.5f;
        this.model.root.scaleZ = 0.5f;
        this.model.root.yaw = -yaw;
        this.model.root.roll = (float) Math.PI;
        this.model.left_wing.roll = this.angle;
        this.model.right_wing.roll = -this.angle;

        this.colorAlpha = Math.min(Math.min(1, (this.age + tickDelta) * 0.01f), (this.maxAge - this.age) * 0.01f);

        VertexConsumer consumer = this.immediate.getBuffer(RenderLayer.getEntityTranslucent(this.texture));
        this.model.render(stack, consumer, this.getBrightness(tickDelta), OverlayTexture.DEFAULT_UV, 1, 1, 1, this.colorAlpha);
        this.immediate.draw();
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    public static class MothFactory implements ParticleFactory<DefaultParticleType> {

        @Nullable
        @Override
        public Particle createParticle(DefaultParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new MothParticle(world, x, y, z, MothModel.createMoth(), MOTH_TEXTURE);
        }
    }

}
