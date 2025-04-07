package shadowshiftstudio.animalinvaders;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import shadowshiftstudio.animalinvaders.entity.ModEntities;
import shadowshiftstudio.animalinvaders.entity.client.bullet.BulletRenderer;
import shadowshiftstudio.animalinvaders.entity.client.potapimmo.PotapimmoRenderer;
import shadowshiftstudio.animalinvaders.entity.client.bobrittobandito.BobrittoBanditoRenderer;
import shadowshiftstudio.animalinvaders.item.ModCreativeModeTabs;
import shadowshiftstudio.animalinvaders.item.ModItems;
import shadowshiftstudio.animalinvaders.biome.ModBiomeModifiers;
import shadowshiftstudio.animalinvaders.effect.ModEffects;
import shadowshiftstudio.animalinvaders.sound.ModSounds;

@Mod(AnimalInvaders.MOD_ID)
public class AnimalInvaders
{
    public static final String MOD_ID = "animalinvaders";
    private static final Logger LOGGER = LogUtils.getLogger();

    public AnimalInvaders(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModEntities.register(modEventBus);
        ModBiomeModifiers.register(modEventBus);
        ModEffects.register(modEventBus); // Register custom effects
        ModSounds.register(modEventBus); // Register custom sounds

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("HELLO FROM COMMON SETUP");

        event.enqueueWork(() -> {
            LOGGER.info("Registering entity spawn rules for Animal Invaders mod");
            LOGGER.info("Delegating Potapimmo spawn registration to ModSpawns class");
        });

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("HELLO from server starting");
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            EntityRenderers.register(ModEntities.POTAPIMMO.get(), PotapimmoRenderer::new);
            EntityRenderers.register(ModEntities.BOBRITO_BANDITO.get(), BobrittoBanditoRenderer::new);
            EntityRenderers.register(ModEntities.BULLET.get(), BulletRenderer::new);
        }
    }
}
