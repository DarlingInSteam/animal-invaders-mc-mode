package shadowshiftstudio.animalinvaders.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.effect.ModEffects;

@Mod.EventBusSubscriber(modid = AnimalInvaders.MOD_ID)
public class EffectEvents {
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        
        // Check if entity has Divine Protection effect
        if (entity.hasEffect(ModEffects.DIVINE_PROTECTION.get())) {
            // Reduce damage by 30%
            float originalDamage = event.getAmount();
            float reducedDamage = originalDamage * 0.7f; // 30% damage reduction
            event.setAmount(reducedDamage);
        }
    }
}