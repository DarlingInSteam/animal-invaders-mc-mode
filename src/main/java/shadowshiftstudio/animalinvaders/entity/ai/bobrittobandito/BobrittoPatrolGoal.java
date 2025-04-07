package shadowshiftstudio.animalinvaders.entity.ai.bobrittobandito;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import shadowshiftstudio.animalinvaders.block.settlement.BobrittoManager;
import shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito.BobrittoBanditoEntity;

import java.util.EnumSet;

/**
 * Цель для патрулирования территории поселения лидером отряда бобритто
 */
public class BobrittoPatrolGoal extends Goal {
    private final BobrittoBanditoEntity bobrito;
    private final double speedModifier;
    private Path path;
    private Vec3 targetPos;
    private final PathNavigation pathNavigation;
    private int tryPathfindingTickCounter;
    private static final int CHECK_PATH_INTERVAL = 20; // Проверка пути каждую секунду

    public BobrittoPatrolGoal(BobrittoBanditoEntity bobrito, double speedModifier) {
        this.bobrito = bobrito;
        this.speedModifier = speedModifier;
        this.pathNavigation = bobrito.getNavigation();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Только патрульные лидеры используют эту цель
        return bobrito.isPatrolLeader() && bobrito.getTarget() == null;
    }

    @Override
    public boolean canContinueToUse() {
        // Продолжаем, пока бобритто остается лидером патруля и не имеет цели для атаки
        return bobrito.isPatrolLeader() && bobrito.getTarget() == null;
    }

    @Override
    public void start() {
        // Ничего не делаем при старте, путь будет создан в tick()
        tryPathfindingTickCounter = 0;
    }

    @Override
    public void stop() {
        this.bobrito.getNavigation().stop();
    }

    @Override
    public void tick() {
        // Если нет текущего пути или путь завершен, пробуем найти новую точку
        if (this.pathNavigation.isDone() || tryPathfindingTickCounter <= 0) {
            targetPos = bobrito.getNextPatrolPoint();
            
            if (targetPos != null) {
                this.path = this.pathNavigation.createPath(
                    new BlockPos((int)targetPos.x, (int)targetPos.y, (int)targetPos.z), 
                    1); // Точность прибытия - 1 блок
                    
                if (this.path != null && this.path.canReach()) {
                    this.pathNavigation.moveTo(this.path, this.speedModifier);
                }
            }
            
            tryPathfindingTickCounter = CHECK_PATH_INTERVAL;
        } else {
            tryPathfindingTickCounter--;
        }
        
        // Иногда путь может быть заблокирован, поэтому проверяем, движется ли бобритто
        if (this.bobrito.getNavigation().isStuck()) {
            tryPathfindingTickCounter = 0; // Это заставит попробовать новый путь на следующем тике
        }
    }
}