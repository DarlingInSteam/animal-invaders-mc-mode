package shadowshiftstudio.animalinvaders.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shadowshiftstudio.animalinvaders.AnimalInvaders;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = 
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, AnimalInvaders.MOD_ID);

    // Register the Divine Protection effect
    public static final RegistryObject<MobEffect> DIVINE_PROTECTION = MOB_EFFECTS.register("divine_protection",
            DivineProtectionEffect::new);
            
    // Register the Last Stand effect
    public static final RegistryObject<MobEffect> LAST_STAND = MOB_EFFECTS.register("last_stand_effect",
            LastStandEffect::new);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}