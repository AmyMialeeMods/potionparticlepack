package xyz.amymialee.potionparticlepack.cca;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import xyz.amymialee.potionparticlepack.PotionParticlePackComponents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StatusComponent implements AutoSyncedComponent {
	private final LivingEntity entity;
	private final Map<StatusEffect, Integer> effects = new HashMap<>();
	private int totalWeight = 0;
	private boolean activeFlag = false;

	public StatusComponent(LivingEntity entity) {
		this.entity = entity;
	}

	public void setEffects(Collection<StatusEffectInstance> effects) {
		this.effects.clear();
		this.totalWeight = 0;
		for (StatusEffectInstance effect : effects) {
			this.effects.put(effect.getEffectType(), effect.getAmplifier() + 1);
			this.totalWeight += effect.getAmplifier() + 1;
		}
		this.activeFlag = true;
		PotionParticlePackComponents.STATUS.sync(this.entity);
	}

	public void clear() {
		this.effects.clear();
		PotionParticlePackComponents.STATUS.sync(this.entity);
	}

	public StatusEffect getRandomEffect() {
		if (this.effects.isEmpty()) return null;
		int random = (int) (this.entity.getRandom().nextDouble() * this.totalWeight);
		for (Map.Entry<StatusEffect, Integer> entry : this.effects.entrySet()) {
			random -= entry.getValue();
			if (random < 0) return entry.getKey();
		}
		return null;
	}

	public float getWeight() {
		return this.totalWeight;
	}

	public boolean isActive() {
		return this.activeFlag;
	}

	@Override
	public void readFromNbt(NbtCompound tag) {
		this.clear();
		int[] effects = tag.getIntArray("effects");
		int[] weights = tag.getIntArray("weights");
		if (effects.length != weights.length) return;
		for (int i = 0; i < effects.length; i++) {
			StatusEffect effect = Registries.STATUS_EFFECT.get(effects[i]);
			if (effect != null) this.effects.put(effect, weights[i]);
		}
		this.totalWeight = tag.getInt("totalWeight");
		this.activeFlag = tag.getBoolean("activeFlag");
	}

	@Override
	public void writeToNbt(NbtCompound tag) {
		tag.putIntArray("effects", new ArrayList<>(this.effects.keySet().stream().map(Registries.STATUS_EFFECT::getRawId).toList()));
		tag.putIntArray("weights", new ArrayList<>(this.effects.values()));
		tag.putInt("totalWeight", this.totalWeight);
		tag.putBoolean("activeFlag", this.activeFlag);
	}
}