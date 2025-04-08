package shadowshiftstudio.animalinvaders.entity.ai.bobrittobandito;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
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
    private int stuckTimer = 0;
    private int teleportCheckTimer = 0;
    private int targetCheckTimer = 0;
    private static final int MAX_STUCK_TIME = 60; // 3 секунды
    private static final int TELEPORT_CHECK_INTERVAL = 40; // 2 секунды
    private static final int TARGET_CHECK_INTERVAL = 10; // Проверка цели каждые 10 тиков (0.5 сек)
    private static final float EMERGENCY_TELEPORT_DISTANCE = 10.0F;

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
        if (!bobrito.isPatrolFollower()) {
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

        // Проверяем атаку на лидера и синхронизируем цели
        checkAndUpdateTarget(leader);

        // Проверить расстояние до лидера - следуем за лидером даже если есть цель,
        // чтобы не отставать от группы во время боя
        double distSq = bobrito.distanceToSqr(leader);
        return distSq > (double)(this.minDist * this.minDist);
    }

    @Override
    public boolean canContinueToUse() {
        // Проверить, все еще ли является последователем
        if (!bobrito.isPatrolFollower()) {
            return false;
        }

        // Проверить, жив ли лидер
        BobrittoBanditoEntity leader = bobrito.getLeader();
        if (leader == null || !leader.isAlive()) {
            return false;
        }

        // Продолжаем следовать, пока не окажемся совсем рядом или не будем в бою
        double distSq = bobrito.distanceToSqr(leader);
        return distSq > 1.0;
    }

    @Override
    public void start() {
        this.leader = bobrito.getLeader();
        this.timeToRecalcPath = 0;
        this.stuckTimer = 0;
        this.teleportCheckTimer = 0;
        this.targetCheckTimer = 0;
        this.oldWaterCost = bobrito.getPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER);
        // Разрешить ходить по воде при следовании за лидером
        bobrito.setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.leader = null;
        this.navigation.stop();
        this.stuckTimer = 0;
        // Вернуть штраф за движение по воде
        bobrito.setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER, this.oldWaterCost);
    }

    /**
     * Проверяет и обновляет цель бобритто, синхронизируя её с целью лидера
     * или реагируя на нападения
     */
    private void checkAndUpdateTarget(BobrittoBanditoEntity leader) {
        // Если на лидера напали, атакуем нападавшего
        if (leader.getLastHurtByMob() != null && leader.getLastHurtByMob().isAlive()) {
            LivingEntity attacker = leader.getLastHurtByMob();
            if (bobrito.getTarget() == null || !bobrito.getTarget().equals(attacker)) {
                bobrito.setTarget(attacker);
                // Делаем бобритто лицом к атакующему
                bobrito.getLookControl().setLookAt(attacker, 30.0F, 30.0F);
                System.out.println("Follower targeting leader's attacker: " + attacker);
            }
        } 
        // Если у лидера есть цель, атакуем ту же цель
        else if (leader.getTarget() != null && leader.getTarget().isAlive()) {
            LivingEntity leaderTarget = leader.getTarget();
            if (bobrito.getTarget() == null || !bobrito.getTarget().equals(leaderTarget)) {
                bobrito.setTarget(leaderTarget);
                // Делаем бобритто лицом к цели лидера
                bobrito.getLookControl().setLookAt(leaderTarget, 30.0F, 30.0F);
                System.out.println("Follower targeting leader's target: " + leaderTarget);
            }
        }
    }

    /**
     * Телепортирует последователя к лидеру, если он застрял или слишком далеко
     */
    private void teleportToLeader() {
        if (this.leader == null) return;
        
        // Генерируем позицию рядом с лидером
        double angle = bobrito.getRandom().nextDouble() * 2 * Math.PI;
        double offsetX = Math.cos(angle) * 2.0; // 2 блока от лидера
        double offsetZ = Math.sin(angle) * 2.0;
        
        BlockPos leaderPos = leader.blockPosition();
        BlockPos targetPos = leaderPos.offset((int)offsetX, 0, (int)offsetZ);
        
        // Находим верхний твердый блок
        BlockPos safePos = bobrito.level().getHeightmapPos(
            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 
            targetPos
        );
        
        // Телепортируем
        bobrito.teleportTo(
            safePos.getX() + 0.5, 
            safePos.getY(), 
            safePos.getZ() + 0.5
        );
        
        // Сбрасываем таймеры
        stuckTimer = 0;
        teleportCheckTimer = 0;
        
        // Делаем бобритто лицом к лидеру после телепортации
        bobrito.getLookControl().setLookAt(leader, 30.0F, 30.0F);
        
        System.out.println("Follower teleported to leader");
    }

    @Override
    public void tick() {
        // Периодически проверяем цели
        if (++targetCheckTimer >= TARGET_CHECK_INTERVAL) {
            targetCheckTimer = 0;
            
            if (this.leader != null) {
                checkAndUpdateTarget(this.leader);
            }
        }
        
        // Проверяем, не нужно ли телепортироваться к лидеру
        if (++teleportCheckTimer >= TELEPORT_CHECK_INTERVAL) {
            teleportCheckTimer = 0;
            
            if (this.leader != null) {
                double distSq = bobrito.distanceToSqr(this.leader);
                
                // Слишком далеко от лидера - телепортируемся
                if (distSq > EMERGENCY_TELEPORT_DISTANCE * EMERGENCY_TELEPORT_DISTANCE) {
                    teleportToLeader();
                    return;
                }
            }
        }
        
        // Смотреть в сторону лидера если нет цели для атаки
        if (this.leader != null) {
            // Если у нас есть цель, смотрим на неё с приоритетом
            if (bobrito.getTarget() != null && bobrito.getTarget().isAlive()) {
                bobrito.getLookControl().setLookAt(bobrito.getTarget(), 30.0F, 30.0F);
                
                // При этом всё равно следуем за лидером, если слишком далеко
                double distToLeader = bobrito.distanceToSqr(this.leader);
                if (distToLeader > this.maxDist * this.maxDist) {
                    // Двигаемся к лидеру даже во время боя, чтобы не отставать от группы
                    if (--this.timeToRecalcPath <= 0) {
                        this.timeToRecalcPath = 10;
                        Path path = this.navigation.createPath(this.leader, 0);
                        if (path != null && path.canReach()) {
                            this.navigation.moveTo(path, this.speedModifier);
                        } else {
                            stuckTimer += 10; // Ускоряем счетчик застревания в бою
                        }
                    }
                }
            } else {
                // Если нет цели, смотрим на лидера и следуем за ним
                bobrito.getLookControl().setLookAt(this.leader, 10.0F, (float)bobrito.getMaxHeadXRot());
            }
            
            // Обновлять путь к лидеру
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10; // Обновлять путь каждые 10 тиков (полсекунды)
                
                double distSq = bobrito.distanceToSqr(this.leader);
                double followDistance = this.minDist * 1.5f;
                
                if (distSq >= followDistance * followDistance) {
                    Path path = this.navigation.createPath(this.leader, 0);
                    boolean canMove = path != null && path.canReach();
                    this.navigation.moveTo(path, this.speedModifier);
                    
                    // Если путь не удается найти или путь заблокирован
                    if (!canMove || this.navigation.isStuck()) {
                        stuckTimer++;
                        
                        // Если застряли на слишком долгое время - телепортируемся
                        if (stuckTimer >= MAX_STUCK_TIME) {
                            teleportToLeader();
                        }
                    } else {
                        stuckTimer = 0; // Сбрасываем счетчик застревания если успешно двигаемся
                    }
                } else {
                    // Если достаточно близко и нет цели, прекратить движение
                    if (bobrito.getTarget() == null) {
                        this.navigation.stop();
                    }
                    stuckTimer = 0;
                }
            }
        }
    }
}