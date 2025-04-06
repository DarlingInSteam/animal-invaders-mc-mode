package shadowshiftstudio.animalinvaders.entity;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.entity.custom.potapimmo.PotapimmoEntity;
import net.minecraft.world.entity.monster.Monster;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = AnimalInvaders.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSpawns {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final List<ResourceLocation> GREEN_BIOMES = Arrays.asList(
            Biomes.FOREST.location(),
            Biomes.FLOWER_FOREST.location(),
            Biomes.BIRCH_FOREST.location(),
            Biomes.OLD_GROWTH_BIRCH_FOREST.location(),
            Biomes.DARK_FOREST.location(),
            Biomes.JUNGLE.location(),
            Biomes.SPARSE_JUNGLE.location(),
            Biomes.BAMBOO_JUNGLE.location(),
            Biomes.PLAINS.location(),
            Biomes.SUNFLOWER_PLAINS.location(),
            Biomes.MEADOW.location(),
            Biomes.GROVE.location(),
            Biomes.SWAMP.location(),
            Biomes.MANGROVE_SWAMP.location(),
            Biomes.SAVANNA.location(),
            Biomes.SAVANNA_PLATEAU.location(),
            Biomes.WINDSWEPT_SAVANNA.location(),
            Biomes.TAIGA.location(),
            Biomes.OLD_GROWTH_PINE_TAIGA.location(),
            Biomes.OLD_GROWTH_SPRUCE_TAIGA.location()
    );

    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        LOGGER.info("Setting up spawn placement rules for Potapimmo");
        
        event.register(
            ModEntities.POTAPIMMO.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            PotapimmoEntity::checkPotapimmoSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Adding Potapimmo to spawn settings");
            LOGGER.info("Potapimmo spawn configured through SpawnPlacementRegisterEvent");
        });
    }

    public static boolean isGreenBiome(ResourceLocation biomeId) {
        return GREEN_BIOMES.contains(biomeId);
    }
}