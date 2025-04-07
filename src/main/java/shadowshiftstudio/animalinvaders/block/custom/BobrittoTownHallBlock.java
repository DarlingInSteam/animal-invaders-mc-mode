package shadowshiftstudio.animalinvaders.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import shadowshiftstudio.animalinvaders.block.settlement.SettlementManager;

/**
 * The Town Hall is the main building of a Bobrito settlement.
 * It provides influence in a large area (200x200 blocks) where Bobritos can build other structures.
 * Based on a village town hall structure.
 */
public class BobrittoTownHallBlock extends BobrittoSettlementBlock {
    private static final int TOWN_HALL_INFLUENCE_RADIUS = 100; // 200x200 area (100 blocks in each direction)

    public BobrittoTownHallBlock(Properties properties) {
        super(properties, TOWN_HALL_INFLUENCE_RADIUS);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        
        if (!level.isClientSide) {
            // Log the creation of a new town hall
            level.getServer().getPlayerList().broadcastSystemMessage(
                net.minecraft.network.chat.Component.literal("Bobrito settlement established at " + 
                    pos.getX() + ", " + pos.getY() + ", " + pos.getZ()),
                false);
            
            // Check for other town halls too close to this one
            checkForNearbyTownHalls(level, pos);
        }
    }
    
    private void checkForNearbyTownHalls(Level level, BlockPos pos) {
        // Get all town halls in the world
        for (BlockPos otherPos : SettlementManager.getSettlementBlocksOfType(level, BobrittoTownHallBlock.class)) {
            // Skip the current town hall
            if (otherPos.equals(pos)) {
                continue;
            }
            
            // Check if this town hall is too close to another town hall
            double distSq = otherPos.distSqr(pos);
            if (distSq < TOWN_HALL_INFLUENCE_RADIUS * TOWN_HALL_INFLUENCE_RADIUS * 2) { // 2x radius overlap
                level.getServer().getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.literal("Warning: This settlement is too close to another Bobrito settlement!"),
                    false);
                break;
            }
        }
    }
}