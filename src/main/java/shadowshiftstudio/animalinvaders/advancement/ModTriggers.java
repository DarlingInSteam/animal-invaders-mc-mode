package shadowshiftstudio.animalinvaders.advancement;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import shadowshiftstudio.animalinvaders.AnimalInvaders;

@Mod.EventBusSubscriber(modid = AnimalInvaders.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModTriggers {
    // Initialize the trigger when this class is loaded
    private static final LegendarySwordsCollectedTrigger LEGENDARY_SWORDS_COLLECTED = new LegendarySwordsCollectedTrigger();
    
    // Static method to allow access to the trigger
    public static LegendarySwordsCollectedTrigger getLegendarySwordsCollectedTrigger() {
        return LEGENDARY_SWORDS_COLLECTED;
    }

    @SubscribeEvent
    public static void registerTriggers(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Register our custom trigger to Minecraft's criteria system
            CriteriaTriggers.register(LEGENDARY_SWORDS_COLLECTED);
        });
    }
}