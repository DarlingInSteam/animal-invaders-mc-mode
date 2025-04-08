package shadowshiftstudio.animalinvaders.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class LastStandEffect extends MobEffect {
    private static final String DAMAGE_BOOST_ID = "830d0239-7226-4e48-90b8-b52a645ac56c";
    private static final float DAMAGE_BOOST_PERCENT = 0.3f; // 30% increased damage
    private static final float MIN_HEALTH = 10.0f; // 5 hearts (each heart is 2 health points)

    public LastStandEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD700); // Gold color
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, DAMAGE_BOOST_ID, DAMAGE_BOOST_PERCENT, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // If health drops below 5 hearts (10 health points), set it back to 5 hearts
        if (entity.getHealth() < MIN_HEALTH) {
            entity.setHealth(MIN_HEALTH);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Make sure this effect is applied every tick
        return true;
    }
}