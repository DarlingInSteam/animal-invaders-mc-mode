package shadowshiftstudio.animalinvaders.entity.ai.bobrittobandito;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito.BobrittoBanditoEntity;

import java.util.EnumSet;

/**
 * Цель для следования за лидером отряда бобритто
 */
public class BobrittoFollowLeaderGoal extends Goal {
    private final BobrittoBanditoEntity bobrito;
    private BobrittoBanditoEntity leader;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float minDist;
    private final float maxDist;
    private float oldWaterCost;

    public BobrittoFollowLeaderGoal(BobrittoBanditoEntity bobrito, double speedModifier, float minDist, float maxDist) {
        this.bobrito = bobrito;
        this.speedModifier = speedModifier;
        this.navigation = bobrito.getNavigation();
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Только последователи используют эту цель
        if (!bobrito.isPatrolFollower() || bobrito.getTarget() != null) {
            return false;
        }

        // Найти лидера
        BobrittoBanditoEntity leader = bobrito.getLeader();
        if (leader == null) {
            return false;
        }

        // Лидер должен быть жив
        if (!leader.isAlive()) {
            return false;
        }

        // Проверить расстояние до лидера
        double distSq = bobrito.distanceToSqr(leader);
        return distSq > (double)(this.maxDist * this.maxDist);
    }

    @Override
    public boolean canContinueToUse() {
        // Проверить наличие цели атаки
        if (bobrito.getTarget() != null) {
            return false;
        }

        // Проверить, все еще ли является последователем
        if (!bobrito.isPatrolFollower()) {
            return false;
        }

        // Проверить, жив ли лидер
        BobrittoBanditoEntity leader = bobrito.getLeader();
        if (leader == null || !leader.isAlive()) {
            return false;
        }

        // Проверить, достаточно ли близко к лидеру
        double distSq = bobrito.distanceToSqr(leader);
        return distSq > (double)(this.minDist * this.minDist);
    }

    @Override
    public void start() {
        this.leader = bobrito.getLeader();
        this.timeToRecalcPath = 0;
        this.oldWaterCost = bobrito.getPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER);
        // Разрешить ходить по воде при следовании за лидером
        bobrito.setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.leader = null;
        this.navigation.stop();
        // Вернуть штраф за движение по воде
        bobrito.setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        // Смотреть в сторону лидера
        if (this.leader != null) {
            bobrito.getLookControl().setLookAt(this.leader, 10.0F, (float)bobrito.getMaxHeadXRot());
            
            // Обновлять путь к лидеру
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10; // Обновлять путь каждые 10 тиков (полсекунды)
                
                // Если цель атаки есть, прекратить следование
                if (bobrito.getTarget() != null) {
                    return;
                }
                
                // Двигаться к лидеру если слишком далеко
                double distSq = bobrito.distanceToSqr(this.leader);
                double followDistance = this.maxDist * 0.75f;
                
                if (distSq >= followDistance * followDistance) {
                    Path path = this.navigation.createPath(this.leader, 0);
                    this.navigation.moveTo(path, this.speedModifier);
                } else {
                    // Если достаточно близко, прекратить движение
                    this.navigation.stop();
                }
            }
        }
    }
}