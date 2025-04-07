package shadowshiftstudio.animalinvaders.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import shadowshiftstudio.animalinvaders.block.settlement.SettlementManager;

/**
 * Base class for all Bobrito Bandito settlement blocks
 */
public abstract class BobrittoSettlementBlock extends Block {
    protected final int influenceRadius;

    public BobrittoSettlementBlock(Properties properties, int influenceRadius) {
        super(properties);
        this.influenceRadius = influenceRadius;
    }
    
    /**
     * Returns the radius of influence of this settlement block
     */
    public int getInfluenceRadius() {
        return influenceRadius;
    }

    /**
     * Checks if a position is within the influence of this settlement block
     */
    public boolean isWithinInfluence(Level level, BlockPos settlementPos, BlockPos posToCheck) {
        // Simple square influence check
        int xDiff = Math.abs(settlementPos.getX() - posToCheck.getX());
        int zDiff = Math.abs(settlementPos.getZ() - posToCheck.getZ());
        
        return xDiff <= influenceRadius && zDiff <= influenceRadius;
    }
    
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        
        if (!level.isClientSide()) {
            // Register this settlement block with the SettlementManager
            SettlementManager.registerSettlementBlock(level, pos, this);
        }
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide()) {
            // Unregister this settlement block from the SettlementManager
            SettlementManager.unregisterSettlementBlock(level, pos);
        }
        
        super.onRemove(state, level, pos, newState, isMoving);
    }
}