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
        
        // Find a suitable position for the town hall
        BlockPos townHallPos = findBestSuitablePosition(level, worldSpawn, 10, 10, 10);
        if (townHallPos == null) {
            // If we couldn't find a suitable position, log an error
            String errorMsg = "Failed to generate Bobrito settlement - no suitable location found within " + 
                              SPAWN_SEARCH_RADIUS + " blocks of spawn!";
            LOGGER.error(errorMsg);
            level.getServer().getPlayerList().broadcastSystemMessage(
                net.minecraft.network.chat.Component.literal(errorMsg),
                false);
            return;
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
            LOGGER.error("Failed to place town hall structure");
            return;
        }
        
        // Track positions to ensure minimum spacing
        List<BlockPos> buildingPositions = new ArrayList<>();
        buildingPositions.add(townHallPos);
        
        // Find position for house
        BlockPos housePos = findSuitablePositionWithSpacing(level, townHallPos, 7, 7, 7, SETTLEMENT_SPACING, buildingPositions);
        if (housePos != null) {
            LOGGER.info("Building house at X:{}, Y:{}, Z:{}", housePos.getX(), housePos.getY(), housePos.getZ());
            
            if (placeVanillaStructure(level, housePos, HOUSE_STRUCTURE)) {
                // Find the center of the structure and place our house block
                BlockPos centerPos = findStructureCenter(level, housePos, 5, 5);
                level.setBlock(centerPos, ModBlocks.BOBRITO_HOUSE.get().defaultBlockState(), 3);
                
                LOGGER.info("Built house structure with marker block at X:{}, Y:{}, Z:{}", 
                           centerPos.getX(), centerPos.getY(), centerPos.getZ());
                
                buildingPositions.add(housePos);
            } else {
                LOGGER.error("Failed to place house structure");
            }
        } else {
            LOGGER.error("Could not find suitable position for house");
        }
        
        // Find position for barracks
        BlockPos barracksPos = findSuitablePositionWithSpacing(level, townHallPos, 7, 7, 7, SETTLEMENT_SPACING, buildingPositions);
        if (barracksPos != null) {
            LOGGER.info("Building barracks at X:{}, Y:{}, Z:{}", barracksPos.getX(), barracksPos.getY(), barracksPos.getZ());
            
            if (placeVanillaStructure(level, barracksPos, BARRACKS_STRUCTURE)) {
                // Find the center of the structure and place our barracks block
                BlockPos centerPos = findStructureCenter(level, barracksPos, 5, 5);
                level.setBlock(centerPos, ModBlocks.BOBRITO_BARRACKS.get().defaultBlockState(), 3);
                
                LOGGER.info("Built barracks structure with marker block at X:{}, Y:{}, Z:{}", 
                           centerPos.getX(), centerPos.getY(), centerPos.getZ());
            } else {
                LOGGER.error("Failed to place barracks structure");
            }
        } else {
            LOGGER.error("Could not find suitable position for barracks");
        }
        
        // Log settlement creation and broadcast to players
        BlockPos centerPos = findStructureCenter(level, townHallPos, 7, 7);
        String successMsg = "A new Bobrito settlement has spawned at X:" + centerPos.getX() + 
                            ", Y:" + centerPos.getY() + ", Z:" + centerPos.getZ();
        LOGGER.info(successMsg);
        level.getServer().getPlayerList().broadcastSystemMessage(
            net.minecraft.network.chat.Component.literal(successMsg),
            false);
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
                return false;
            }
            
            // Prepare placement settings
            StructurePlaceSettings settings = new StructurePlaceSettings();
            
            // Place the structure
            template.placeInWorld(level, pos, pos, settings, RandomSource.create(), 2);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error placing structure {}: {}", structureId, e.getMessage());
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
    private static BlockPos findBestSuitablePosition(ServerLevel level, BlockPos center, int sizeX, int sizeY, int sizeZ) {
        BlockPos bestPos = null;
        double closestDistSq = Double.MAX_VALUE;
        
        LOGGER.info("Searching for suitable position within {} blocks of spawn", SPAWN_SEARCH_RADIUS);
        
        // Check in expanding circles from spawn
        for (int radius = 20; radius <= SPAWN_SEARCH_RADIUS; radius += 20) {
            LOGGER.debug("Searching at radius: {}", radius);
            
            for (int attempt = 0; attempt < 8; attempt++) {
                // Try positions in a circle around center
                double angle = 2 * Math.PI * attempt / 8.0;
                int offX = (int) (Math.cos(angle) * radius);
                int offZ = (int) (Math.sin(angle) * radius);
                
                BlockPos basePos = center.offset(offX, 0, offZ);
                BlockPos pos = findSuitableFlatGround(level, basePos, sizeX, sizeY, sizeZ);
                
                if (pos != null) {
                    double distSq = pos.distSqr(center);
                    if (distSq < closestDistSq) {
                        closestDistSq = distSq;
                        bestPos = pos;
                        LOGGER.debug("Found better position at X:{}, Y:{}, Z:{}, distance: {}", 
                                    pos.getX(), pos.getY(), pos.getZ(), Math.sqrt(distSq));
                    }
                }
            }
            
            // If we found a suitable position and it's reasonably close, return it
            if (bestPos != null && Math.sqrt(closestDistSq) < 200) {
                LOGGER.info("Found good position at distance {} blocks", Math.sqrt(closestDistSq));
                return bestPos;
            }
        }
        
        // If we've completed the full search, return the best position found (if any)
        if (bestPos != null) {
            LOGGER.info("Completed full search, best position at distance {} blocks", Math.sqrt(closestDistSq));
        }
        return bestPos;
    }
    
    /**
     * Find a suitable flat ground position with no obstacles
     */
    private static BlockPos findSuitableFlatGround(ServerLevel level, BlockPos center, int sizeX, int sizeY, int sizeZ) {
        int groundY = findGroundLevel(level, center);
        if (groundY == -1) return null;
        
        BlockPos basePos = center.above(groundY);
        
        // Check if area is flat and clear
        if (!isAreaFlatAndClear(level, basePos, sizeX, sizeY, sizeZ)) {
            return null;
        }
        
        return basePos;
    }
    
    /**
     * Find a suitable position with minimum spacing from other buildings
     */
    private static BlockPos findSuitablePositionWithSpacing(ServerLevel level, BlockPos center, 
                                                          int sizeX, int sizeY, int sizeZ, 
                                                          int minSpacing, List<BlockPos> existingBuildings) {
        Random rand = new Random();
        
        for (int attempt = 0; attempt < MAX_SEARCH_ATTEMPTS; attempt++) {
            // Try random positions in a circle around the center
            double angle = rand.nextDouble() * 2 * Math.PI;
            int distance = minSpacing + rand.nextInt(10); // Random distance between minSpacing and minSpacing+10
            int offX = (int) (Math.cos(angle) * distance);
            int offZ = (int) (Math.sin(angle) * distance);
            
            BlockPos searchPos = center.offset(offX, 0, offZ);
            BlockPos pos = findSuitableFlatGround(level, searchPos, sizeX, sizeY, sizeZ);
            
            if (pos != null) {
                // Check if it's far enough from all existing buildings
                boolean tooClose = false;
                for (BlockPos existing : existingBuildings) {
                    if (pos.distSqr(existing) < minSpacing * minSpacing) {
                        tooClose = true;
                        break;
                    }
                }
                
                if (!tooClose) {
                    return pos;
                }
            }
        }
        
        return null; // No suitable position found
    }
    
    /**
     * Check if an area is flat and clear of obstacles
     */
    private static boolean isAreaFlatAndClear(ServerLevel level, BlockPos basePos, int sizeX, int sizeY, int sizeZ) {
        // Check if the base is on solid ground
        if (!isSolid(level.getBlockState(basePos.below()))) {
            return false;
        }
        
        // Check if there's enough flat, clear space
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                BlockPos checkPos = basePos.offset(x, -1, z);
                
                // Check if ground is at same level (mostly flat)
                if (!isSolid(level.getBlockState(checkPos))) {
                    return false;
                }
                
                // Check for obstructions
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