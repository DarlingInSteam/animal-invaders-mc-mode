package shadowshiftstudio.animalinvaders.events;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.entity.ModEntities;
import shadowshiftstudio.animalinvaders.entity.ModSpawns;
import shadowshiftstudio.animalinvaders.entity.custom.potapimmo.PotapimmoEntity;

import java.util.Random;

/**
 * Класс для принудительного спавна потапиммо в мире
 */
@Mod.EventBusSubscriber(modid = AnimalInvaders.MOD_ID)
public class ForceSpawnEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random random = new Random();
    private static int tickCounter = 0;
    private static final int SPAWN_FREQUENCY = 1200;
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        tickCounter++;
        
        if (tickCounter >= SPAWN_FREQUENCY) {
            tickCounter = 0;
            
            try {
                ServerLevel serverLevel = event.getServer().overworld();
                
                if (!serverLevel.players().isEmpty()) {
                    var player = serverLevel.players().get(random.nextInt(serverLevel.players().size()));
                    
                    for (int attempts = 0; attempts < 10; attempts++) {
                        // Генерируем случайный угол в радианах (0-2π)
                        double angle = random.nextDouble() * Math.PI * 2;
                        // Генерируем случайное расстояние между 32 и 64 блоками
                        double distance = 32 + random.nextDouble() * 32;
                        
                        // Переводим в координаты X и Z
                        int offsetX = (int)(Math.cos(angle) * distance);
                        int offsetZ = (int)(Math.sin(angle) * distance);
                        
                        BlockPos spawnPos = player.blockPosition().offset(offsetX, 0, offsetZ);
                        
                        spawnPos = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos);
                        
                        ResourceLocation biomeId = serverLevel.getBiome(spawnPos).unwrapKey()
                                .map(key -> key.location())
                                .orElse(null);
                        
                        if (biomeId != null && ModSpawns.isGreenBiome(biomeId)) {
                            RandomSource randomSource = RandomSource.create();
                            if (PotapimmoEntity.checkPotapimmoSpawnRules(
                                    ModEntities.POTAPIMMO.get(), serverLevel, MobSpawnType.NATURAL, spawnPos, randomSource)) {
                                
                                int groupSize = 3 + random.nextInt(3); // 3-5
                                int successfulSpawns = 0;
                                
                                for (int i = 0; i < groupSize; i++) {
                                    try {
                                        BlockPos entityPos = spawnPos.offset(
                                                random.nextInt(7) - 3,
                                                0,
                                                random.nextInt(7) - 3);
                                        
                                        entityPos = serverLevel.getHeightmapPos(
                                                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, entityPos);
                                        
                                        EntityType<PotapimmoEntity> entityType = ModEntities.POTAPIMMO.get();
                                        PotapimmoEntity potapimmo = null;
                                        
                                        try {
                                            potapimmo = entityType.create(serverLevel);
                                            
                                            if (potapimmo == null) {
                                                LOGGER.error("Failed to create Potapimmo entity, EntityType.create returned null");
                                                continue;
                                            }
                                        } catch (Exception e) {
                                            LOGGER.error("Exception while creating Potapimmo entity: {}", e.getMessage());
                                            e.printStackTrace();
                                            continue;
                                        }
                                        
                                        potapimmo.moveTo(
                                                entityPos.getX() + 0.5,
                                                entityPos.getY(),
                                                entityPos.getZ() + 0.5,
                                                random.nextFloat() * 360.0F,
                                                0.0F);
                                        
                                        potapimmo.finalizeSpawn(
                                                serverLevel,
                                                serverLevel.getCurrentDifficultyAt(entityPos),
                                                MobSpawnType.NATURAL,
                                                null,
                                                null);
                                        
                                        serverLevel.addFreshEntityWithPassengers(potapimmo);
                                        successfulSpawns++;
                                        LOGGER.info("Spawned Potapimmo at {} in biome {}",
                                                entityPos, biomeId);
                                    } catch (Exception e) {
                                        LOGGER.error("Exception during Potapimmo spawn process: {}", e.getMessage());
                                        e.printStackTrace();
                                    }
                                }
                                
                                if (successfulSpawns > 0) {
                                    LOGGER.info("Successfully spawned {} Potapimmo entities", successfulSpawns);
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Caught exception in onServerTick: {}", e.getMessage());
                e.printStackTrace();
                
                tickCounter = -SPAWN_FREQUENCY / 2;
            }
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        LOGGER.info("ForceSpawnEvents initialized - Potapimmo will forcibly spawn in green biomes");
        LOGGER.info("Registered biomes for Potapimmo spawning:");
        
        ForgeRegistries.BIOMES.getEntries().forEach(entry -> {
            ResourceLocation biomeId = entry.getKey().location();
            if (ModSpawns.isGreenBiome(biomeId)) {
                LOGGER.info(" - {}", biomeId);
            }
        });
        
        LOGGER.info("Force spawn system will attempt to spawn Potapimmo groups every {} ticks", SPAWN_FREQUENCY);
    }
}