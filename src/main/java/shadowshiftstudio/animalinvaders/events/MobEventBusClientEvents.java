package shadowshiftstudio.animalinvaders.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadowshiftstudio.animalinvaders.entity.client.bobrittobandito.BobrittoBanditoLeaderModel;
import shadowshiftstudio.animalinvaders.entity.client.bobrittobandito.BobrittoBanditoLeaderModelLayers;
import shadowshiftstudio.animalinvaders.entity.client.bobrittobandito.BobrittoBanditoModel;
import shadowshiftstudio.animalinvaders.entity.client.bobrittobandito.BobrittoBanditoModelLayers;
import shadowshiftstudio.animalinvaders.entity.client.potapimmo.PotapimmoModel;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.common.Mod;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.entity.client.potapimmo.PotapimmoModelLayers;

@Mod.EventBusSubscriber(modid = AnimalInvaders.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MobEventBusClientEvents {
    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PotapimmoModelLayers.POTAPIMMO_LAYER, PotapimmoModel::createBodyLayer);
        event.registerLayerDefinition(BobrittoBanditoModelLayers.BOBRITTO_LAYER, BobrittoBanditoModel::createBodyLayer);
        event.registerLayerDefinition(BobrittoBanditoLeaderModelLayers.BOBRITTO_LEADER_LAYER, BobrittoBanditoLeaderModel::createBodyLayer);
    }
}
