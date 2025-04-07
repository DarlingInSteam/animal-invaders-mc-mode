package shadowshiftstudio.animalinvaders.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import shadowshiftstudio.animalinvaders.block.settlement.SettlementManager;

/**
 * Barracks building for Bobrito Banditos.
 * These buildings will eventually spawn military Bobritos.
 * Based on village armorer structures.
 */
public class BobrittoBarracksBlock extends BobrittoSettlementBlock {
    private static final int BARRACKS_INFLUENCE_RADIUS = 20; // Medium influence area

    public BobrittoBarracksBlock(Properties properties) {
        super(properties, BARRACKS_INFLUENCE_RADIUS);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        
        if (!level.isClientSide) {
            // Find the town hall this barracks belongs to
            BlockPos townHall = SettlementManager.findNearestTownHall(level, pos);
            if (townHall != null) {
                level.getServer().getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.literal("Bobrito barracks built! Military operations available."),
                    false);
            }
        }
    }
}