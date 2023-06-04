package xyz.amymialee.potionparticlepack.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.VibrationParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.event.PositionSource;

public class StatusEffectParticleEffect implements ParticleEffect {
    public static final ParticleEffect.Factory<StatusEffectParticleEffect> PARAMETERS_FACTORY = new ParticleEffect.Factory<>() {
        @Override
        public StatusEffectParticleEffect read(ParticleType<StatusEffectParticleEffect> particleType, StringReader stringReader) throws CommandSyntaxException {
            stringReader.expect(' ');
            Identifier identifier = Identifier.fromCommandInput(stringReader);
            return new StatusEffectParticleEffect(particleType, identifier);
        }

        @Override
        public StatusEffectParticleEffect read(ParticleType<StatusEffectParticleEffect> particleType, PacketByteBuf packetByteBuf) {
            return new StatusEffectParticleEffect(particleType, packetByteBuf.readIdentifier());
        }
    };
    private final ParticleType<StatusEffectParticleEffect> type;
    private final Identifier statusEffect;

    public static Codec<StatusEffectParticleEffect> createCodec(ParticleType<StatusEffectParticleEffect> type) {
        return Identifier.CODEC.xmap(state -> new StatusEffectParticleEffect(type, Registries.STATUS_EFFECT.get(state)), effect -> effect.statusEffect);
    }

    public StatusEffectParticleEffect(ParticleType<StatusEffectParticleEffect> type, StatusEffect statusEffect) {
        this.type = type;
        this.statusEffect = Registries.STATUS_EFFECT.getId(statusEffect);
    }

    public StatusEffectParticleEffect(ParticleType<StatusEffectParticleEffect> type, Identifier statusEffect) {
        this.type = type;
        this.statusEffect = statusEffect;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(this.statusEffect);
    }

    @Override
    public String asString() {
        return this.statusEffect.toString();
    }

    @Override
    public ParticleType<StatusEffectParticleEffect> getType() {
        return this.type;
    }

    public Identifier getStatusEffect() {
        return this.statusEffect;
    }
}