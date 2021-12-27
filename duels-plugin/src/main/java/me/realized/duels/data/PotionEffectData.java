package me.realized.duels.data;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectData {

    public static PotionEffectData fromPotionEffect(final PotionEffect effect) {
        return new PotionEffectData(effect);
    }

    private String type;
    private int duration;
    private int amplifier;

    private PotionEffectData() {}

    private PotionEffectData(final PotionEffect effect) {
        this.type = effect.getType().getName();
        this.duration = effect.getDuration();
        this.amplifier = effect.getAmplifier();
    }

    public PotionEffect toPotionEffect() {
        final PotionEffectType effectType = PotionEffectType.getByName(type);

        if (effectType == null) {
            return null;
        }

        return new PotionEffect(effectType, duration, amplifier);
    }
}
