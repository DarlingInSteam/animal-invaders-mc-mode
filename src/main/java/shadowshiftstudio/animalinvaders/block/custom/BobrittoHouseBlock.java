package shadowshiftstudio.animalinvaders.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import shadowshiftstudio.animalinvaders.block.settlement.SettlementManager;

/**
 * Residential building for Bobrito Banditos.
 * Each house provides capacity for 5 Bobritos.
 * Based on village house structures.
 */
public class BobrittoHouseBlock extends BobrittoSettlementBlock {
    private static final int HOUSE_INFLUENCE_RADIUS = 10; // Small local influence
    public static final int BOBRITO_CAPACITY = 5; // Each house can host 5 Bobritos

    public BobrittoHouseBlock(Properties properties) {
        super(properties, HOUSE_INFLUENCE_RADIUS);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        
        if (!level.isClientSide) {
            // Increase settlement capacity
            BlockPos townHall = SettlementManager.findNearestTownHall(level, pos);
            if (townHall != null) {
                SettlementManager.increaseSettlementCapacity(level, townHall, BOBRITO_CAPACITY);
                level.getServer().getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.literal("Bobrito house built! Settlement capacity increased by " + BOBRITO_CAPACITY),
                    false);
            }
        }
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide) {
            // Decrease settlement capacity before unregistering
            BlockPos townHall = SettlementManager.findNearestTownHall(level, pos);
            if (townHall != null) {
                SettlementManager.decreaseSettlementCapacity(level, townHall, BOBRITO_CAPACITY);
            }
        }
        
        super.onRemove(state, level, pos, newState, isMoving);
    }
}