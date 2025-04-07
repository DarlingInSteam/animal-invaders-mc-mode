package shadowshiftstudio.animalinvaders.entity.ai.bobrittobandito;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito.BobrittoBanditoEntity;

import javax.annotation.Nullable;

/**
 * Цель для блуждания обычных бобритто в пределах поселения
 */
public class BobrittoSettlementWanderGoal extends RandomStrollGoal {
    private final BobrittoBanditoEntity bobrito;
    private static final int SETTLEMENT_RADIUS = 15; // Радиус перемещения внутри поселения

    public BobrittoSettlementWanderGoal(BobrittoBanditoEntity bobrito, double speedModifier) {
        super(bobrito, speedModifier);
        this.bobrito = bobrito;
    }

    @Override
    public boolean canUse() {
        // Только обычные бобритто используют эту цель (не патрульные)
        if (bobrito.isPatrolLeader() || bobrito.isPatrolFollower() || bobrito.getTarget() != null) {
            return false;
        }
        
        // Если нет центра поселения, используем обычную логику блуждания
        if (bobrito.getSettlementCenter() == null) {
            return super.canUse();
        }
        
        // Иначе переопределяем супер метод, но с привязкой к центру поселения
        if (this.mob.isVehicle()) {
            return false;
        } else {
            if (!this.forceTrigger) {
                if (this.mob.getRandom().nextInt(this.interval) != 0) {
                    return false;
                }
            }

            Vec3 vec3 = this.getPosition();
            if (vec3 == null) {
                return false;
            } else {
                this.wantedX = vec3.x;
                this.wantedY = vec3.y;
                this.wantedZ = vec3.z;
                this.forceTrigger = false;
                return true;
            }
        }
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        // Если нет центра поселения, используем обычную логику блуждания
        if (bobrito.getSettlementCenter() == null) {
            return super.getPosition();
        }
        
        // Иначе генерируем случайную позицию в пределах поселения
        BlockPos center = bobrito.getSettlementCenter();
        if (center != null) {
            // Генерируем позицию рядом с центром поселения
            return LandRandomPos.getPos(this.mob, SETTLEMENT_RADIUS, SETTLEMENT_RADIUS
            );
        }
        
        return super.getPosition();
    }
}