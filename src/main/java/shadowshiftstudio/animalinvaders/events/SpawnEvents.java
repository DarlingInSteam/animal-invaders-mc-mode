package shadowshiftstudio.animalinvaders.events;

import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.entity.ModEntities;
import shadowshiftstudio.animalinvaders.entity.ModSpawns;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = AnimalInvaders.MOD_ID)
public class SpawnEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @Mod.EventBusSubscriber(modid = AnimalInvaders.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {
        
        @SubscribeEvent
        public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
            ModSpawns.registerSpawnPlacements(event);
        }
        
        @SubscribeEvent
        public static void onLoadComplete(FMLLoadCompleteEvent event) {
            LOGGER.info("FML Load Complete - Animal Invaders mod initialized");
        }
    }
}