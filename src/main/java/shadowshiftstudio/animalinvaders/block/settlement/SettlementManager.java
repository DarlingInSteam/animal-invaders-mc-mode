package shadowshiftstudio.animalinvaders.block.settlement;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import shadowshiftstudio.animalinvaders.block.ModBlocks;
import shadowshiftstudio.animalinvaders.block.custom.BobrittoSettlementBlock;
import shadowshiftstudio.animalinvaders.block.custom.BobrittoTownHallBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for managing Bobrito Bandito settlements.
 * This class will handle tracking all settlement blocks and their influence areas.
 */
public class SettlementManager {
    // Tracks all settlement blocks in each dimension by their position
    private static final Map<Level, Map<BlockPos, BobrittoSettlementBlock>> settlementBlocks = new HashMap<>();
    
    // Tracks settlement capacity by town hall position
    private static final Map<Level, Map<BlockPos, Integer>> settlementCapacity = new HashMap<>();
    
    /**
     * Register a settlement block at the given position
     */
    public static void registerSettlementBlock(Level level, BlockPos pos, BobrittoSettlementBlock block) {
        if (level.isClientSide()) {
            return; // Only track on server
        }
        
        settlementBlocks.computeIfAbsent(level, k -> new HashMap<>()).put(pos, block);
        
        // Initialize capacity for town halls
        if (block instanceof BobrittoTownHallBlock) {
            settlementCapacity.computeIfAbsent(level, k -> new HashMap<>()).put(pos, 0);
        }
    }
    
    /**
     * Unregister a settlement block at the given position
     */
    public static void unregisterSettlementBlock(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        
        Map<BlockPos, BobrittoSettlementBlock> blocks = settlementBlocks.get(level);
        if (blocks != null) {
            BobrittoSettlementBlock block = blocks.get(pos);
            blocks.remove(pos);
            
            // Remove capacity tracking for town halls
            if (block instanceof BobrittoTownHallBlock) {
                Map<BlockPos, Integer> capacities = settlementCapacity.get(level);
                if (capacities != null) {
                    capacities.remove(pos);
                }
            }
        }
    }
    
    /**
     * Increase the capacity of a settlement
     */
    public static void increaseSettlementCapacity(Level level, BlockPos townHallPos, int amount) {
        if (level.isClientSide()) {
            return;
        }
        
        Map<BlockPos, Integer> capacities = settlementCapacity.get(level);
        if (capacities != null && capacities.containsKey(townHallPos)) {
            int currentCapacity = capacities.get(townHallPos);
            capacities.put(townHallPos, currentCapacity + amount);
        }
    }
    
    /**
     * Decrease the capacity of a settlement
     */
    public static void decreaseSettlementCapacity(Level level, BlockPos townHallPos, int amount) {
        if (level.isClientSide()) {
            return;
        }
        
        Map<BlockPos, Integer> capacities = settlementCapacity.get(level);
        if (capacities != null && capacities.containsKey(townHallPos)) {
            int currentCapacity = capacities.get(townHallPos);
            capacities.put(townHallPos, Math.max(0, currentCapacity - amount));
        }
    }
    
    /**
     * Get the current capacity of a settlement
     */
    public static int getSettlementCapacity(Level level, BlockPos townHallPos) {
        if (level.isClientSide()) {
            return 0;
        }
        
        Map<BlockPos, Integer> capacities = settlementCapacity.get(level);
        if (capacities != null && capacities.containsKey(townHallPos)) {
            return capacities.get(townHallPos);
        }
        
        return 0;
    }
    
    /**
     * Check if the given position is within the influence of any town hall
     */
    public static boolean isWithinTownHallInfluence(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return false; // Only check on server
        }
        
        Map<BlockPos, BobrittoSettlementBlock> blocks = settlementBlocks.get(level);
        if (blocks == null) {
            return false;
        }
        
        // Check all town halls
        for (Map.Entry<BlockPos, BobrittoSettlementBlock> entry : blocks.entrySet()) {
            if (entry.getValue() instanceof BobrittoTownHallBlock) {
                BobrittoTownHallBlock townHall = (BobrittoTownHallBlock) entry.getValue();
                if (townHall.isWithinInfluence(level, entry.getKey(), pos)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Find the nearest town hall to the given position
     */
    public static BlockPos findNearestTownHall(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return null; // Only check on server
        }
        
        Map<BlockPos, BobrittoSettlementBlock> blocks = settlementBlocks.get(level);
        if (blocks == null) {
            return null;
        }
        
        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        
        // Find the nearest town hall
        for (Map.Entry<BlockPos, BobrittoSettlementBlock> entry : blocks.entrySet()) {
            if (entry.getValue() instanceof BobrittoTownHallBlock) {
                double distSq = entry.getKey().distSqr(pos);
                if (distSq < nearestDistSq) {
                    nearest = entry.getKey();
                    nearestDistSq = distSq;
                }
            }
        }
        
        return nearest;
    }
    
    /**
     * Get all settlement blocks of a specific type
     */
    public static List<BlockPos> getSettlementBlocksOfType(Level level, Class<? extends BobrittoSettlementBlock> blockClass) {
        List<BlockPos> result = new ArrayList<>();
        
        Map<BlockPos, BobrittoSettlementBlock> blocks = settlementBlocks.get(level);
        if (blocks != null) {
            for (Map.Entry<BlockPos, BobrittoSettlementBlock> entry : blocks.entrySet()) {
                if (blockClass.isInstance(entry.getValue())) {
                    result.add(entry.getKey());
                }
            }
        }
        
        return result;
    }
}