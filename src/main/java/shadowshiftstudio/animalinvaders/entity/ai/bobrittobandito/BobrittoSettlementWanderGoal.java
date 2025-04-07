package shadowshiftstudio.animalinvaders.entity.ai.bobrittobandito;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito.BobrittoBanditoEntity;

import javax.annotation.Nullable;

/**
 * Цель для блуждания обычных бобритто в пределах поселения
 */
public class BobrittoSettlementWanderGoal extends RandomStrollGoal {
    private final BobrittoBanditoEntity bobrito;
    private static final int SETTLEMENT_RADIUS = 15; // Радиус перемещения внутри поселения
    private static final int MOVEMENT_INTERVAL = 60; // Частота смены направления в тиках (3 секунды)
    private static final int MAX_DISTANCE_FROM_CENTER = 25; // Максимальное расстояние от центра поселения (блоки)
    private Vec3 lastPosition; // Последняя позиция для проверки, не вышли ли за пределы

    public BobrittoSettlementWanderGoal(BobrittoBanditoEntity bobrito, double speedModifier) {
        super(bobrito, speedModifier, MOVEMENT_INTERVAL);
        this.bobrito = bobrito;
        this.interval = MOVEMENT_INTERVAL; // Переопределяем интервал из родительского класса
    }

    @Override
    public boolean canUse() {
        // Только обычные бобритто используют эту цель (не патрульные)
        if (bobrito.isPatrolLeader() || bobrito.isPatrolFollower() || bobrito.getTarget() != null) {
            return false;
        }
        
        // Проверяем, не слишком ли далеко мы от центра поселения
        if (isTooFarFromSettlement()) {
            // Если слишком далеко, немедленно активируем цель, чтобы вернуться
            this.forceTrigger = true;
        }
        
        // Если нет центра поселения, используем обычную логику блуждания,
        // но с более редкими интервалами перемещения
        if (bobrito.getSettlementCenter() == null) {
            return this.mob.getRandom().nextInt(this.interval * 2) == 0;
        }
        
        // Иначе переопределяем логику, но с привязкой к центру поселения
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
                this.lastPosition = new Vec3(bobrito.getX(), bobrito.getY(), bobrito.getZ());
                return true;
            }
        }
    }

    /**
     * Проверяет, не слишком ли далеко бобритто от центра поселения
     */
    private boolean isTooFarFromSettlement() {
        BlockPos center = bobrito.getSettlementCenter();
        if (center == null) {
            return false;
        }
        
        double distanceSq = center.distSqr(bobrito.blockPosition());
        return distanceSq > MAX_DISTANCE_FROM_CENTER * MAX_DISTANCE_FROM_CENTER;
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        // Если нет центра поселения, используем обычную логику блуждания
        if (bobrito.getSettlementCenter() == null) {
            return super.getPosition();
        }
        
        // Если мы слишком далеко от поселения, возвращаемся к центру
        if (isTooFarFromSettlement()) {
            BlockPos center = bobrito.getSettlementCenter();
            Vec3 centerVec = new Vec3(center.getX(), center.getY(), center.getZ());
            
            // Направление к центру поселения
            Vec3 direction = centerVec.subtract(bobrito.position()).normalize();
            
            // Генерируем точку примерно в направлении центра поселения
            return LandRandomPos.getPosTowards(
                this.mob,
                SETTLEMENT_RADIUS,
                SETTLEMENT_RADIUS / 2,
                direction
            );
        }
        
        // Иначе генерируем случайную позицию в пределах поселения
        BlockPos center = bobrito.getSettlementCenter();
        if (center != null) {
            // Генерируем случайную позицию в пределах SETTLEMENT_RADIUS от центра
            Vec3 randomPos = null;
            
            // Пробуем найти хорошую позицию в пределах деревни несколько раз
            for (int attempts = 0; attempts < 5 && randomPos == null; attempts++) {
                // Ограничиваем дистанцию, чтобы не выходить за пределы поселения
                randomPos = LandRandomPos.getPos(
                    this.mob, 
                    Math.min(SETTLEMENT_RADIUS, MAX_DISTANCE_FROM_CENTER - 5), 
                    3
                );
                
                // Проверяем, не слишком ли далеко полученная позиция от центра
                if (randomPos != null) {
                    double distanceSq = new BlockPos(
                        (int)randomPos.x, 
                        (int)randomPos.y, 
                        (int)randomPos.z
                    ).distSqr(center);
                    
                    if (distanceSq > SETTLEMENT_RADIUS * SETTLEMENT_RADIUS) {
                        // Отклоняем точку, если она слишком далеко от центра
                        randomPos = null;
                    }
                }
            }
            
            // Если не удалось найти позицию, возвращаемся ближе к центру
            if (randomPos == null) {
                Vec3 centerVec = Vec3.atBottomCenterOf(center);
                Vec3 directionToCenter = centerVec.subtract(bobrito.position()).normalize();
                
                // Идём примерно в направлении центра, но не точно к нему
                return DefaultRandomPos.getPosTowards(
                    this.mob, 
                    8, 
                    3, 
                    directionToCenter, 
                    0.3
                );
            }
            
            return randomPos;
        }
        
        // Используем стандартный метод, если ничего не подошло
        return super.getPosition();
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // Дополнительная проверка во время движения:
        // если бобритто вышел за пределы деревни, немедленно направляем его обратно
        if (this.lastPosition != null && bobrito.getSettlementCenter() != null) {
            Vec3 currentPos = new Vec3(bobrito.getX(), bobrito.getY(), bobrito.getZ());
            
            // Проверяем, не слишком ли мы далеко от центра
            if (isTooFarFromSettlement()) {
                // Сбрасываем текущий путь и заставляем сущность найти новый путь к центру
                this.stop();
                this.forceTrigger = true;
                
                // Записываем факт, что сущность вышла за пределы деревни
                if (bobrito.getRandom().nextInt(10) == 0) { // лог только в 10% случаев, чтобы не спамить
                    System.out.println("Bobrito left settlement area, forcing return to center");
                }
            }
            
            this.lastPosition = currentPos;
        }
    }
    
    @Override
    public boolean canContinueToUse() {
        // Если бобритто попал в бой, прекращаем блуждание
        if (bobrito.getTarget() != null) {
            return false;
        }
        
        // Дополнительная проверка, чтобы не выходить за границы поселения
        if (bobrito.getSettlementCenter() != null && isTooFarFromSettlement()) {
            // Если вышли слишком далеко, прекращаем текущее блуждание и начинаем новое (к центру)
            return false;
        }
        
        return super.canContinueToUse();
    }
}