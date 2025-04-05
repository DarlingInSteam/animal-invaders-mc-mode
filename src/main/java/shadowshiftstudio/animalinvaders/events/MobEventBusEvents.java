package shadowshiftstudio.animalinvaders.events;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.entity.ModEntities;
import shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito.BobrittoBanditoEntity;
import shadowshiftstudio.animalinvaders.entity.custom.potapimmo.PotapimmoEntity;

@Mod.EventBusSubscriber(modid = AnimalInvaders.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MobEventBusEvents {
    @SubscribeEvent
    public static void registerAttribute(EntityAttributeCreationEvent event) {
        event.put(ModEntities.POTAPIMMO.get(), PotapimmoEntity.createAttributes().build());
        event.put(ModEntities.BOBRITO_BANDITO.get(), BobrittoBanditoEntity.createAttributes().build());
    }
}
