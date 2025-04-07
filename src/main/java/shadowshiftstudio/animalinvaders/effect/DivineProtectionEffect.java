package shadowshiftstudio.animalinvaders.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;

public class DivineProtectionEffect extends MobEffect {
    public DivineProtectionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD700); // Gold color for divine protection
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // This method is called every tick while the effect is active
        // Remove harmful effects
        entity.removeEffect(MobEffects.POISON);
        entity.removeEffect(MobEffects.WITHER);
        entity.removeEffect(MobEffects.HARM);
        entity.removeEffect(MobEffects.HUNGER);
        entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        entity.removeEffect(MobEffects.DIG_SLOWDOWN);
        entity.removeEffect(MobEffects.WEAKNESS);
        entity.removeEffect(MobEffects.CONFUSION);
        entity.removeEffect(MobEffects.BLINDNESS);
        entity.removeEffect(MobEffects.LEVITATION);
        entity.removeEffect(MobEffects.UNLUCK);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Make sure this effect is applied every tick
        return true;
    }
}