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
    private int stuckCheckCounter = 0;
    private int resumeFromCombatTimer = 0;
    
    private static final int CHECK_PATH_INTERVAL = 20; // Проверка пути каждую секунду
    private static final int STUCK_THRESHOLD = 60; // 3 секунды максимума застревания
    private static final int RESUME_PATROL_DELAY = 40; // 2 секунды после боя для возобновления патрулирования

    public BobrittoPatrolGoal(BobrittoBanditoEntity bobrito, double speedModifier) {
        this.bobrito = bobrito;
        this.speedModifier = speedModifier;
        this.pathNavigation = bobrito.getNavigation();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Только патрульные лидеры используют эту цель
        if (!bobrito.isPatrolLeader()) {
            return false;
        }
        
        // Если бобритто в бою, сбрасываем флаг истечения времени возобновления патруля
        if (bobrito.getTarget() != null) {
            resumeFromCombatTimer = RESUME_PATROL_DELAY;
            return false;
        }
        
        // Если бобритто только что вышел из боя, дадим ему немного времени
        if (resumeFromCombatTimer > 0) {
            resumeFromCombatTimer--;
            return false;
        }
        
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        // Продолжаем, пока бобритто остается лидером патруля и не имеет цели для атаки
        if (!bobrito.isPatrolLeader() || bobrito.getTarget() != null) {
            return false;
        }
        
        return true;
    }

    @Override
    public void start() {
        // Сбрасываем значения при старте
        tryPathfindingTickCounter = 0;
        stuckCheckCounter = 0;
    }

    @Override
    public void stop() {
        this.bobrito.getNavigation().stop();
        
        // Если остановка из-за боя, отметим время для паузы перед возобновлением патрулирования
        if (bobrito.getTarget() != null) {
            resumeFromCombatTimer = RESUME_PATROL_DELAY;
        }
    }

    /**
     * Проверяет, застрял ли бобритто, и меняет точку патрулирования при необходимости
     */
    private void checkStuck() {
        if (this.bobrito.getNavigation().isStuck()) {
            stuckCheckCounter++;
            
            if (stuckCheckCounter >= STUCK_THRESHOLD) {
                // Если долго застряли, пробуем найти другую точку патрулирования
                BlockPos settlementCenter = bobrito.getSettlementCenter();
                if (settlementCenter != null) {
                    // Генерируем точку в текущем режиме патрулирования
                    if (bobrito.getPatrolState() == BobrittoManager.PatrolState.PATROLLING_OUTER) {
                        // Внешнее патрулирование - увеличиваем счетчик, чтобы перейти к следующей точке
                        bobrito.incrementPatrolPoint();
                    } else {
                        // Внутреннее патрулирование - генерируем новую случайную точку
                        double angle = bobrito.getRandom().nextDouble() * 2 * Math.PI;
                        int radius = 15; // Меньший радиус для патрулирования внутри
                        int x = settlementCenter.getX() + (int)(Math.cos(angle) * bobrito.getRandom().nextInt(radius));
                        int z = settlementCenter.getZ() + (int)(Math.sin(angle) * bobrito.getRandom().nextInt(radius));
                        int y = bobrito.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z);
                        
                        targetPos = new Vec3(x, y, z);
                        path = this.pathNavigation.createPath(new BlockPos(x, y, z), 1);
                        if (path != null && path.canReach()) {
                            this.pathNavigation.moveTo(path, this.speedModifier);
                        }
                    }
                }
                
                // Сбрасываем счетчик
                stuckCheckCounter = 0;
                tryPathfindingTickCounter = 0;
            }
        } else {
            // Если не застряли, сбрасываем счетчик
            stuckCheckCounter = 0;
        }
    }

    @Override
    public void tick() {
        // Проверяем, не застрял ли бобритто
        checkStuck();
        
        // Если нет текущего пути или путь завершен, пробуем найти новую точку
        if (this.pathNavigation.isDone() || tryPathfindingTickCounter <= 0) {
            targetPos = bobrito.getNextPatrolPoint();
            
            if (targetPos != null) {
                BlockPos targetBlock = new BlockPos((int)targetPos.x, (int)targetPos.y, (int)targetPos.z);
                
                // Проверяем, что путь ведет к подходящей поверхности
                targetBlock = bobrito.level().getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 
                    targetBlock
                );
                
                this.path = this.pathNavigation.createPath(targetBlock, 1);
                    
                if (this.path != null && this.path.canReach()) {
                    this.pathNavigation.moveTo(this.path, this.speedModifier);
                } else {
                    // Если не можем найти путь, увеличиваем счетчик застревания
                    stuckCheckCounter += 10; // Быстрее перейдем к другой точке
                }
            }
            
            tryPathfindingTickCounter = CHECK_PATH_INTERVAL;
        } else {
            tryPathfindingTickCounter--;
        }
    }
}