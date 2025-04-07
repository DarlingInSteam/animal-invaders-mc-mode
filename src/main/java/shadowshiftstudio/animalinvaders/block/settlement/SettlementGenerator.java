package shadowshiftstudio.animalinvaders.block.settlement;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.block.ModBlocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles the generation of Bobrito settlements using vanilla Minecraft structures
 */
@Mod.EventBusSubscriber(modid = AnimalInvaders.MOD_ID)
public class SettlementGenerator {
    private static final int SPAWN_SEARCH_RADIUS = 1000; // Search radius around spawn (blocks)
    private static final int SETTLEMENT_SPACING = 25; // Spacing between buildings (blocks)
    private static final int MAX_SEARCH_ATTEMPTS = 200; // Max attempts to find suitable locations
    
    // Structure IDs for vanilla village buildings
    private static final ResourceLocation TOWN_HALL_STRUCTURE = new ResourceLocation("minecraft:village/plains/houses/plains_big_house_1");
    private static final ResourceLocation TOWN_HALL_FALLBACK = new ResourceLocation("minecraft:village/plains/houses/plains_big_house_1");
    private static final ResourceLocation HOUSE_STRUCTURE = new ResourceLocation("minecraft:village/plains/houses/plains_small_house_1");
    private static final ResourceLocation BARRACKS_STRUCTURE = new ResourceLocation("minecraft:village/plains/houses/plains_armorer_house_1");
    
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SettlementGenerator.class);

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        ServerLevel level = server.getLevel(Level.OVERWORLD);

        LOGGER.info("Checking if Bobrito settlement needs to be generated...");
        
        // Only generate if no town halls exist yet
        List<BlockPos> existingTownHalls = SettlementManager.getSettlementBlocksOfType(level, 
                shadowshiftstudio.animalinvaders.block.custom.BobrittoTownHallBlock.class);
        
        if (level != null && existingTownHalls.isEmpty()) {
            LOGGER.info("No existing Bobrito settlements found. Generating new settlement...");
            generateInitialSettlement(level);
        } else {
            if (!existingTownHalls.isEmpty()) {
                BlockPos pos = existingTownHalls.get(0);
                LOGGER.info("Existing Bobrito settlement found at X:{}, Y:{}, Z:{}", pos.getX(), pos.getY(), pos.getZ());
                
                // Broadcast the location to players
                level.getServer().getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.literal("Existing Bobrito settlement found at " + 
                        pos.getX() + ", " + pos.getY() + ", " + pos.getZ()),
                    false);
            }
        }
    }

    /**
     * Generate the initial Bobrito settlement at spawn
     */
    private static void generateInitialSettlement(ServerLevel level) {
        // Get world spawn
        BlockPos worldSpawn = level.getSharedSpawnPos();
        LOGGER.info("World spawn is at X:{}, Y:{}, Z:{}", worldSpawn.getX(), worldSpawn.getY(), worldSpawn.getZ());
        
        // Detect if this is a superflat world
        boolean isSuperflat = isSuperFlatWorld(level);
        LOGGER.info("Is superflat world: {}", isSuperflat);
        
        // Find a suitable position for the town hall
        BlockPos townHallPos = findBestSuitablePosition(level, worldSpawn, 10, 10, 10, isSuperflat);
        if (townHallPos == null) {
            // If we couldn't find a suitable position, try direct spawn point for superflat
            if (isSuperflat) {
                townHallPos = new BlockPos(worldSpawn.getX(), worldSpawn.getY(), worldSpawn.getZ());
                LOGGER.info("Using direct spawn position for superflat world");
            } else {
                // For regular worlds, log error and exit
                String errorMsg = "Failed to generate Bobrito settlement - no suitable location found within " + 
                                  SPAWN_SEARCH_RADIUS + " blocks of spawn!";
                LOGGER.error(errorMsg);
                level.getServer().getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.literal(errorMsg),
                    false);
                return;
            }
        }
        
        LOGGER.info("Found suitable location for Bobrito town hall at X:{}, Y:{}, Z:{}", 
                   townHallPos.getX(), townHallPos.getY(), townHallPos.getZ());
        
        // Place the town hall structure
        boolean townHallSuccess = placeVanillaStructure(level, townHallPos, TOWN_HALL_STRUCTURE);
        if (townHallSuccess) {
            // Find the center of the structure and place our town hall block
            BlockPos centerPos = findStructureCenter(level, townHallPos, 7, 7);
            level.setBlock(centerPos, ModBlocks.BOBRITO_TOWN_HALL.get().defaultBlockState(), 3);
            
            LOGGER.info("Built town hall structure with marker block at X:{}, Y:{}, Z:{}", 
                       centerPos.getX(), centerPos.getY(), centerPos.getZ());
        } else {
            // Fallback for superflat if structure placement fails
            if (isSuperflat) {
                level.setBlock(townHallPos, ModBlocks.BOBRITO_TOWN_HALL.get().defaultBlockState(), 3);
                LOGGER.info("Placed only town hall block (without structure) at X:{}, Y:{}, Z:{}", 
                           townHallPos.getX(), townHallPos.getY(), townHallPos.getZ());
            } else {
                LOGGER.error("Failed to place town hall structure");
                return;
            }
        }
        
        // Track positions to ensure minimum spacing
        List<BlockPos> buildingPositions = new ArrayList<>();
        buildingPositions.add(townHallPos);
        
        // Build three houses instead of one to spawn 15 Bobritos (5 per house)
        LOGGER.info("Generating three houses to accommodate 15 Bobritos...");
        for (int i = 0; i < 3; i++) {
            // Find position for house
            BlockPos housePos = findSuitablePositionWithSpacing(level, townHallPos, 7, 7, 7, SETTLEMENT_SPACING, buildingPositions, isSuperflat);
            if (housePos != null) {
                LOGGER.info("Building house #{} at X:{}, Y:{}, Z:{}", i+1, housePos.getX(), housePos.getY(), housePos.getZ());
                
                boolean houseSuccess = placeVanillaStructure(level, housePos, HOUSE_STRUCTURE);
                if (houseSuccess) {
                    // Find the center of the structure and place our house block
                    BlockPos centerPos = findStructureCenter(level, housePos, 5, 5);
                    level.setBlock(centerPos, ModBlocks.BOBRITO_HOUSE.get().defaultBlockState(), 3);
                    
                    LOGGER.info("Built house structure #{} with marker block at X:{}, Y:{}, Z:{}", 
                               i+1, centerPos.getX(), centerPos.getY(), centerPos.getZ());
                } else if (isSuperflat) {
                    // Fallback for superflat
                    level.setBlock(housePos, ModBlocks.BOBRITO_HOUSE.get().defaultBlockState(), 3);
                    LOGGER.info("Placed only house block #{} (without structure) at X:{}, Y:{}, Z:{}", 
                               i+1, housePos.getX(), housePos.getY(), housePos.getZ());
                } else {
                    LOGGER.error("Failed to place house structure #{}", i+1);
                }
                
                buildingPositions.add(housePos);
            } else {
                LOGGER.error("Could not find suitable position for house #{}", i+1);
            }
        }
        
        // Find position for barracks
        BlockPos barracksPos = findSuitablePositionWithSpacing(level, townHallPos, 7, 7, 7, SETTLEMENT_SPACING, buildingPositions, isSuperflat);
        if (barracksPos != null) {
            LOGGER.info("Building barracks at X:{}, Y:{}, Z:{}", barracksPos.getX(), barracksPos.getY(), barracksPos.getZ());
            
            boolean barracksSuccess = placeVanillaStructure(level, barracksPos, BARRACKS_STRUCTURE);
            if (barracksSuccess) {
                // Find the center of the structure and place our barracks block
                BlockPos centerPos = findStructureCenter(level, barracksPos, 5, 5);
                level.setBlock(centerPos, ModBlocks.BOBRITO_BARRACKS.get().defaultBlockState(), 3);
                
                LOGGER.info("Built barracks structure with marker block at X:{}, Y:{}, Z:{}", 
                           centerPos.getX(), centerPos.getY(), centerPos.getZ());
            } else if (isSuperflat) {
                // Fallback for superflat
                level.setBlock(barracksPos, ModBlocks.BOBRITO_BARRACKS.get().defaultBlockState(), 3);
                LOGGER.info("Placed only barracks block (without structure) at X:{}, Y:{}, Z:{}",
                           barracksPos.getX(), barracksPos.getY(), barracksPos.getZ());
            } else {
                LOGGER.error("Failed to place barracks structure");
            }
        } else {
            LOGGER.error("Could not find suitable position for barracks");
        }
        
        // Log settlement creation and broadcast to players
        BlockPos centerPos = (townHallSuccess) 
            ? findStructureCenter(level, townHallPos, 7, 7) 
            : townHallPos;
            
        String successMsg = "A new Bobrito settlement has spawned at X:" + centerPos.getX() + 
                            ", Y:" + centerPos.getY() + ", Z:" + centerPos.getZ() +
                            " with 3 houses (15 Bobritos) and a patrol group!";
        LOGGER.info(successMsg);
        level.getServer().getPlayerList().broadcastSystemMessage(
            net.minecraft.network.chat.Component.literal(successMsg),
            false);
            
        // Создаем начальных бобритто для каждого дома
        spawnInitialBobritos(level);
    }
    
    /**
     * Spawn initial Bobrito for each house in the settlement
     */
    private static void spawnInitialBobritos(ServerLevel level) {
        // Получаем все дома бобритто в поселении
        List<BlockPos> houses = SettlementManager.getSettlementBlocksOfType(level, 
                shadowshiftstudio.animalinvaders.block.custom.BobrittoHouseBlock.class);
        
        if (houses.isEmpty()) {
            LOGGER.warn("No Bobrito houses found, cannot spawn initial Bobritos");
            return;
        }
        
        // Получаем ратушу, чтобы использовать её как центр поселения
        List<BlockPos> townHalls = SettlementManager.getSettlementBlocksOfType(level, 
                shadowshiftstudio.animalinvaders.block.custom.BobrittoTownHallBlock.class);
        
        if (townHalls.isEmpty()) {
            LOGGER.warn("No town hall found, cannot spawn initial Bobritos");
            return;
        }
        
        BlockPos townHall = townHalls.get(0);
        LOGGER.info("Creating initial Bobritos for settlement centered at X:{}, Y:{}, Z:{}", 
                townHall.getX(), townHall.getY(), townHall.getZ());
        
        // Для каждого дома создаем 5 бобритто
        for (BlockPos house : houses) {
            LOGGER.info("Spawning 5 Bobritos for house at X:{}, Y:{}, Z:{}", 
                    house.getX(), house.getY(), house.getZ());
            
            // Создаем 5 бобритто вокруг каждого дома
            for (int i = 0; i < 5; i++) {
                try {
                    // Сначала найдем позицию на уровне земли у дома
                    BlockPos houseLevelPos = new BlockPos(house.getX(), 
                                                        findGroundLevelAround(level, house), 
                                                        house.getZ());
                    
                    // Генерируем позицию вокруг дома на уровне земли
                    BlockPos spawnPos = houseLevelPos.offset(
                            (int)(Math.random() * 7) - 3,
                            0,
                            (int)(Math.random() * 7) - 3);
                    
                    // Находим точную позицию на поверхности земли для этих координат
                    BlockPos groundPos = findGroundSurfacePosition(level, spawnPos);
                    if (groundPos == null) {
                        LOGGER.warn("Could not find ground surface at X:{}, Y:{}, Z:{}", 
                                spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                        continue;
                    }
                    
                    // Создаем сущность бобритто
                    shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito.BobrittoBanditoEntity bobrito = 
                            shadowshiftstudio.animalinvaders.entity.ModEntities.BOBRITO_BANDITO.get().create(level);
                    
                    if (bobrito != null) {
                        // Устанавливаем позицию и угол поворота
                        bobrito.moveTo(
                                groundPos.getX() + 0.5,
                                groundPos.getY() + 0.1, // Немного выше земли
                                groundPos.getZ() + 0.5,
                                (float)(Math.random() * 360.0F),
                                0.0F);
                        
                        // Устанавливаем центр поселения для бобритто
                        bobrito.setSettlementCenter(townHall);
                        
                        // Завершаем инициализацию сущности
                        bobrito.finalizeSpawn(
                                level,
                                level.getCurrentDifficultyAt(groundPos),
                                net.minecraft.world.entity.MobSpawnType.STRUCTURE,
                                null,
                                null);
                        
                        // Добавляем сущность в мир
                        level.addFreshEntityWithPassengers(bobrito);
                        LOGGER.info("Spawned Bobrito at X:{}, Y:{}, Z:{}", 
                                groundPos.getX(), groundPos.getY(), groundPos.getZ());
                    } else {
                        LOGGER.error("Failed to create Bobrito entity");
                    }
                } catch (Exception e) {
                    LOGGER.error("Error spawning Bobrito: {}", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        // Обновляем патрульные группы
        LOGGER.info("Triggering patrol group formation...");
        BobrittoManager.updateSettlementPatrols(level);
    }
    
    /**
     * Находит позицию на поверхности земли для указанных координат
     */
    private static BlockPos findGroundSurfacePosition(ServerLevel level, BlockPos pos) {
        // Сначала проверяем, находимся ли мы на земле
        if (isSolid(level.getBlockState(pos.below())) && 
            (level.getBlockState(pos).isAir() || isReplaceable(level.getBlockState(pos)))) {
            return pos;
        }
        
        // Если мы на крыше или в воздухе, ищем землю ниже
        for (int y = pos.getY(); y > level.getMinBuildHeight() + 1; y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (isSolid(level.getBlockState(checkPos.below())) && 
                (level.getBlockState(checkPos).isAir() || isReplaceable(level.getBlockState(checkPos)))) {
                return checkPos;
            }
        }
        
        // Если не нашли землю ниже, пробуем найти выше (на случай если были под землей)
        for (int y = pos.getY() + 1; y < level.getMaxBuildHeight() - 1; y++) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (isSolid(level.getBlockState(checkPos.below())) && 
                (level.getBlockState(checkPos).isAir() || isReplaceable(level.getBlockState(checkPos)))) {
                return checkPos;
            }
        }
        
        return null; // Не удалось найти подходящую позицию
    }
    
    /**
     * Находит уровень земли вокруг здания
     */
    private static int findGroundLevelAround(ServerLevel level, BlockPos buildingPos) {
        // Ищем уровень земли в нескольких точках вокруг здания
        int totalY = 0;
        int validPoints = 0;
        
        int[][] offsets = {
            {5, 0}, {-5, 0}, {0, 5}, {0, -5},
            {5, 5}, {-5, 5}, {5, -5}, {-5, -5}
        };
        
        for (int[] offset : offsets) {
            BlockPos checkPos = buildingPos.offset(offset[0], 0, offset[1]);
            
            // Ищем уровень земли
            BlockPos groundPos = level.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, checkPos);
            
            // Проверяем, действительно ли это земля
            if (groundPos != null && isSolid(level.getBlockState(groundPos.below()))) {
                totalY += groundPos.getY();
                validPoints++;
            }
        }
        
        if (validPoints > 0) {
            return totalY / validPoints;
        } else {
            // Если не нашли ни одной точки, используем уровень ниже здания
            return Math.max(level.getMinBuildHeight() + 1, buildingPos.getY() - 2);
        }
    }
    
    /**
     * Detects if the world is a superflat world
     */
    private static boolean isSuperFlatWorld(ServerLevel level) {
        // Get world spawn
        BlockPos spawn = level.getSharedSpawnPos();
        
        // Log the Y coordinate for debugging
        LOGGER.info("Checking superflat at spawn Y={}", spawn.getY());
        
        // Check 1: Very low Y spawn is often indicative of superflat worlds
        if (spawn.getY() < -50) {
            LOGGER.info("Detected likely superflat world due to low Y spawn at {}", spawn.getY());
            return true;
        }
        
        // Check 2: Direct check for bedrock layer pattern typical in superflat
        boolean hasBedrockLayer = true;
        for (int x = -2; x <= 2 && hasBedrockLayer; x += 2) {
            for (int z = -2; z <= 2 && hasBedrockLayer; z += 2) {
                BlockPos checkPos = new BlockPos(spawn.getX() + x, level.getMinBuildHeight(), spawn.getZ() + z);
                if (!level.getBlockState(checkPos).is(Blocks.BEDROCK)) {
                    hasBedrockLayer = false;
                }
            }
        }
        
        if (hasBedrockLayer) {
            LOGGER.info("Detected superflat world due to bedrock layer pattern");
            return true;
        }
        
        // Check 3: Original flat terrain check (slightly modified)
        int flatCount = 0;
        int checkRadius = 10;
        int checkTotal = 0;
        int prevHeight = -1;
        
        for (int x = -checkRadius; x <= checkRadius; x += 5) {
            for (int z = -checkRadius; z <= checkRadius; z += 5) {
                checkTotal++;
                
                // Find the highest non-air block
                int height = -1;
                for (int y = Math.min(level.getMaxBuildHeight() - 1, 120); y > level.getMinBuildHeight(); y--) {
                    BlockPos checkPos = new BlockPos(spawn.getX() + x, y, spawn.getZ() + z);
                    BlockState state = level.getBlockState(checkPos);
                    if (!state.isAir()) {
                        height = y;
                        break;
                    }
                }
                
                if (height != -1) {
                    if (prevHeight == -1) {
                        prevHeight = height;
                        flatCount++;
                    } else if (Math.abs(height - prevHeight) <= 1) { // Allow small variations
                        flatCount++;
                    }
                }
            }
        }
        
        boolean isFlatTerrain = checkTotal > 0 && (double)flatCount / checkTotal > 0.8;
        
        if (isFlatTerrain) {
            LOGGER.info("Detected superflat world due to flat terrain - flatCount={}, checkTotal={}", flatCount, checkTotal);
            return true;
        }
        
        return false;
    }
    
    /**
     * Place a vanilla Minecraft structure at the specified position
     */
    private static boolean placeVanillaStructure(ServerLevel level, BlockPos pos, ResourceLocation structureId) {
        try {
            StructureTemplateManager templateManager = level.getStructureManager();
            StructureTemplate template = templateManager.getOrCreate(structureId);
            
            if (template == null) {
                LOGGER.error("Failed to load structure template: {}", structureId);
                
                // If this is the town hall structure, try the fallback
                if (structureId.equals(TOWN_HALL_STRUCTURE)) {
                    LOGGER.info("Attempting to use fallback structure for town hall");
                    return placeVanillaStructure(level, pos, TOWN_HALL_FALLBACK);
                }
                
                return false;
            }
            
            // Prepare placement settings
            StructurePlaceSettings settings = new StructurePlaceSettings();
            
            // Place the structure
            template.placeInWorld(level, pos, pos, settings, RandomSource.create(), 2);
            LOGGER.info("Successfully placed structure: {}", structureId);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error placing structure {}: {}", structureId, e.getMessage());
            
            // If this is the town hall structure, try the fallback
            if (structureId.equals(TOWN_HALL_STRUCTURE)) {
                LOGGER.info("Attempting to use fallback structure for town hall after error");
                return placeVanillaStructure(level, pos, TOWN_HALL_FALLBACK);
            }
            
            return false;
        }
    }
    
    /**
     * Find the center of a structure (approximate)
     */
    private static BlockPos findStructureCenter(ServerLevel level, BlockPos basePos, int sizeX, int sizeZ) {
        // For structures, we can estimate the center based on size
        int centerX = basePos.getX() + sizeX / 2;
        int centerZ = basePos.getZ() + sizeZ / 2;
        
        // Find the ground level at this position
        for (int y = basePos.getY() + 1; y <= basePos.getY() + 4; y++) {
            BlockPos potentialPos = new BlockPos(centerX, y, centerZ);
            BlockState state = level.getBlockState(potentialPos);
            BlockState stateBelow = level.getBlockState(potentialPos.below());
            
            // Look for a position with a solid block below and air/replaceable above
            if (isSolid(stateBelow) && (state.isAir() || isReplaceable(state))) {
                return potentialPos;
            }
        }
        
        // If we couldn't find a good center, just use a position 1 block above base
        return basePos.above();
    }

    /**
     * Find the best suitable position as close to spawn as possible
     */
    private static BlockPos findBestSuitablePosition(ServerLevel level, BlockPos center, int sizeX, int sizeY, int sizeZ, boolean isSuperflat) {
        BlockPos bestPos = null;
        double closestDistSq = Double.MAX_VALUE;
        
        LOGGER.info("Searching for suitable position within {} blocks of spawn", SPAWN_SEARCH_RADIUS);
        
        if (isSuperflat) {
            // For superflat, just find a position near spawn
            for (int attempt = 0; attempt < 20; attempt++) {
                int offsetX = (attempt % 10) * 5; // 0, 5, 10, 15...
                int offsetZ = (attempt / 10) * 5; // 0, 0, 0... then 5, 5, 5...
                
                // Alternate between positive and negative offsets
                if (attempt % 2 == 1) offsetX = -offsetX;
                if ((attempt / 2) % 2 == 1) offsetZ = -offsetZ;
                
                BlockPos pos = new BlockPos(
                    center.getX() + offsetX,
                    center.getY(),
                    center.getZ() + offsetZ
                );
                
                // Find the ground level
                int groundY = findGroundLevel(level, pos);
                if (groundY != -1) {
                    // If ground found, adjust Y position
                    pos = new BlockPos(pos.getX(), center.getY() + groundY, pos.getZ());
                    
                    // Simple check for superflat - just make sure the block below is solid
                    if (isSolid(level.getBlockState(pos.below()))) {
                        LOGGER.info("Found suitable position for superflat at X:{}, Y:{}, Z:{}", 
                                   pos.getX(), pos.getY(), pos.getZ());
                        return pos;
                    }
                }
            }
            
            // If no suitable position found, just return the center with adjusted Y
            int groundY = findGroundLevel(level, center);
            if (groundY != -1) {
                LOGGER.info("Using adjusted spawn position for superflat world");
                return new BlockPos(center.getX(), center.getY() + groundY, center.getZ());
            } else {
                LOGGER.info("Using direct spawn position for superflat world");
                return center;
            }
        }
        
        // For normal worlds, check in expanding circles
        Random random = new Random();
        for (int radius = 20; radius <= SPAWN_SEARCH_RADIUS; radius += 20) {
            // Try multiple angles at each radius
            for (int angle = 0; angle < 360; angle += 30) {
                // Convert angle to radians and calculate position
                double rad = Math.toRadians(angle);
                int offsetX = (int) (Math.cos(rad) * radius);
                int offsetZ = (int) (Math.sin(rad) * radius);
                
                BlockPos basePos = new BlockPos(
                    center.getX() + offsetX,
                    center.getY(),
                    center.getZ() + offsetZ
                );
                
                // Find ground level at this position
                int groundY = findGroundLevel(level, basePos);
                if (groundY == -1) continue; // No suitable ground
                
                // Adjust Y position based on ground level
                basePos = new BlockPos(basePos.getX(), center.getY() + groundY, basePos.getZ());
                
                // Check if this position is suitable for the structure
                if (isSuitableForStructure(level, basePos, sizeX, sizeY, sizeZ)) {
                    // Check if this is the closest suitable position to center
                    double distSq = basePos.distSqr(center);
                    if (distSq < closestDistSq) {
                        bestPos = basePos;
                        closestDistSq = distSq;
                        
                        // If we're very close to spawn, just use this position
                        if (distSq < 400) { // 20 blocks
                            return bestPos;
                        }
                    }
                }
            }
            
            // If we found a suitable position at this radius, return it
            if (bestPos != null) {
                return bestPos;
            }
        }
        
        return bestPos; // May be null if no suitable position found
    }
    
    /**
     * Find a suitable position within a certain radius of a center position, while maintaining minimum spacing from other buildings
     */
    private static BlockPos findSuitablePositionWithSpacing(ServerLevel level, BlockPos center, 
                                                         int sizeX, int sizeY, int sizeZ,
                                                         int spacing, List<BlockPos> existingBuildings,
                                                         boolean isSuperflat) {
        LOGGER.info("Finding suitable position near X:{}, Y:{}, Z:{} with spacing {}", 
                   center.getX(), center.getY(), center.getZ(), spacing);
        
        Random random = new Random();
        
        if (isSuperflat) {
            // For superflat worlds, just pick positions in a simple pattern
            // Try different direction offsets from the town hall
            int[][] directions = {
                {1, 0}, {0, 1}, {-1, 0}, {0, -1},  // Cardinal
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}  // Diagonal
            };
            
            // Shuffle directions for variety
            for (int i = directions.length - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int[] temp = directions[i];
                directions[i] = directions[j];
                directions[j] = temp;
            }
            
            // Try each direction with increasing distance
            for (int dist = spacing; dist <= spacing * 3; dist += spacing) {
                for (int[] dir : directions) {
                    int offsetX = dir[0] * dist;
                    int offsetZ = dir[1] * dist;
                    
                    BlockPos pos = new BlockPos(
                        center.getX() + offsetX,
                        center.getY(),
                        center.getZ() + offsetZ
                    );
                    
                    // Find ground level
                    int groundY = findGroundLevel(level, pos);
                    if (groundY != -1) {
                        pos = new BlockPos(pos.getX(), center.getY() + groundY, pos.getZ());
                    }
                    
                    // Check for spacing from existing buildings
                    boolean tooClose = false;
                    for (BlockPos building : existingBuildings) {
                        if (pos.distSqr(building) < spacing * spacing) {
                            tooClose = true;
                            break;
                        }
                    }
                    
                    if (!tooClose && isSolid(level.getBlockState(pos.below()))) {
                        LOGGER.info("Found suitable position for superflat building at X:{}, Y:{}, Z:{}", 
                                   pos.getX(), pos.getY(), pos.getZ());
                        return pos;
                    }
                }
            }
            
            // Fallback for superflat - if all else fails, just place it randomly nearby
            for (int attempt = 0; attempt < 10; attempt++) {
                int offsetX = (int) (random.nextInt(spacing * 3) - (spacing * 1.5));
                int offsetZ = (int) (random.nextInt(spacing * 3) - (spacing * 1.5));
                
                BlockPos pos = new BlockPos(
                    center.getX() + offsetX,
                    center.getY(),
                    center.getZ() + offsetZ
                );
                
                // Simple check for minimum spacing
                boolean tooClose = false;
                for (BlockPos building : existingBuildings) {
                    if (pos.distSqr(building) < spacing * spacing * 0.5) { // Allow closer placement as a fallback
                        tooClose = true;
                        break;
                    }
                }
                
                if (!tooClose) {
                    LOGGER.info("Using fallback position for superflat building at X:{}, Y:{}, Z:{}", 
                               pos.getX(), pos.getY(), pos.getZ());
                    return pos;
                }
            }
        } else {
            // For normal worlds, use a more sophisticated approach
            // Search in expanding circles
            for (int radius = spacing; radius <= spacing * 4; radius += spacing / 2) {
                for (int angleStep = 0; angleStep < 16; angleStep++) {
                    double angle = (Math.PI * 2 * angleStep / 16) + (random.nextDouble() * 0.2);
                    
                    int offsetX = (int) (Math.cos(angle) * radius);
                    int offsetZ = (int) (Math.sin(angle) * radius);
                    
                    BlockPos basePos = new BlockPos(
                        center.getX() + offsetX,
                        center.getY(),
                        center.getZ() + offsetZ
                    );
                    
                    // Find ground level
                    int groundY = findGroundLevel(level, basePos);
                    if (groundY == -1) continue;
                    
                    basePos = new BlockPos(basePos.getX(), center.getY() + groundY, basePos.getZ());
                    
                    // Check for spacing from existing buildings
                    boolean tooClose = false;
                    for (BlockPos building : existingBuildings) {
                        if (basePos.distSqr(building) < spacing * spacing) {
                            tooClose = true;
                            break;
                        }
                    }
                    
                    if (!tooClose && isSuitableForStructure(level, basePos, sizeX, sizeY, sizeZ)) {
                        return basePos;
                    }
                }
            }
        }
        
        // If we reach here, we couldn't find a suitable position
        LOGGER.warn("Could not find suitable position with spacing after {} attempts", MAX_SEARCH_ATTEMPTS);
        return null;
    }
    
    /**
     * Check if a position is suitable for a structure
     */
    private static boolean isSuitableForStructure(ServerLevel level, BlockPos basePos, int sizeX, int sizeY, int sizeZ) {
        // Check ground is solid and relatively flat
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                BlockPos checkPos = basePos.offset(x, -1, z);
                
                // Check if ground is solid
                if (!isSolid(level.getBlockState(checkPos))) {
                    return false;
                }
                
                // Check for obstructions in the building area
                for (int y = 0; y < sizeY; y++) {
                    BlockPos airPos = basePos.offset(x, y, z);
                    if (!isReplaceable(level.getBlockState(airPos))) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Check if a block can be replaced when building
     */
    private static boolean isReplaceable(BlockState state) {
        return state.isAir() || 
               state.is(Blocks.GRASS) || 
               state.is(Blocks.TALL_GRASS) || 
               state.is(Blocks.SEAGRASS) || 
               state.is(Blocks.SNOW) || 
               state.is(Blocks.WATER) && state.getFluidState().getAmount() < 8;
    }
    
    /**
     * Find the ground level at a position
     */
    private static int findGroundLevel(BlockGetter level, BlockPos pos) {
        int maxY = level.getHeight();
        
        // Start from a reasonable height and scan down
        for (int y = Math.min(maxY - 1, 120); y > level.getMinBuildHeight() + 10; y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            BlockState state = level.getBlockState(checkPos);
            BlockState stateAbove = level.getBlockState(checkPos.above());
            
            if (!state.isAir() && isSolid(state) && (stateAbove.isAir() || isReplaceable(stateAbove))) {
                // Don't build on water
                if (state.is(Blocks.WATER)) {
                    continue;
                }
                return y - pos.getY() + 1; // Return offset from the base position
            }
        }
        
        return -1; // No suitable ground found
    }
    
    /**
     * Check if a block is suitable as foundation
     */
    private static boolean isSolid(BlockState state) {
        // Consider common solid blocks as suitable foundation
        return state.is(Blocks.GRASS_BLOCK) || 
               state.is(Blocks.DIRT) || 
               state.is(Blocks.COARSE_DIRT) || 
               state.is(Blocks.PODZOL) || 
               state.is(Blocks.STONE) || 
               state.is(Blocks.GRAVEL) || 
               state.is(Blocks.SAND) ||
               state.is(Blocks.GRANITE) ||
               state.is(Blocks.DIORITE) ||
               state.is(Blocks.ANDESITE) ||
               state.is(Blocks.DEEPSLATE) ||
               state.is(Blocks.TUFF) ||
               state.isSolid();
    }
}