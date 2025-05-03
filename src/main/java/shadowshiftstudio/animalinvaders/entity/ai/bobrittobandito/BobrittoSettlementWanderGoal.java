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
    private static final int SETTLEMENT_RADIUS = 70; // Радиус перемещения внутри поселения
    private static final int MOVEMENT_INTERVAL = 60; // Частота смены направления в тиках (3 секунды)
    private static final int MAX_DISTANCE_FROM_CENTER = 300; // Максимальное расстояние от центра поселения (блоки)
    private Vec3 lastPosition; // Последняя позиция для проверки, не вышли ли за пределы
    private boolean isReturningToSettlement = false; // Флаг, указывающий, что бобритто возвращается в поселение
    private int failedReturnAttempts = 0; // Счетчик неудачных попыток вернуться
    
    // Новые поля для улучшенной телепортации
    private boolean teleportCheckActive = false; // Флаг активного процесса проверки перед телепортацией
    private int teleportCheckTimer = 0; // Таймер для ожидания между проверками (в тиках)
    private double lastSettlementDistance = 0; // Последнее измеренное расстояние до центра поселения
    private static final int TELEPORT_CHECK_DELAY = 100; // 5 секунд (100 тиков)

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
        
        // Особая обработка для бобритто, вышедших за пределы поселения
        if (isTooFarFromSettlement()) {
            // Принудительно активируем цель для возвращения в поселение
            this.forceTrigger = true;
            this.isReturningToSettlement = true;
            
            // Если мы делаем слишком много попыток, телепортируем бобритто обратно в поселение
            if (failedReturnAttempts > 5) {
                // Телепортация в центр поселения при слишком большом количестве неудачных попыток
                teleportToSettlement();
                return false; // После телепортации не нужно использовать эту цель
            }
        } else {
            // Если мы в пределах поселения, сбрасываем флаги
            this.isReturningToSettlement = false;
            this.failedReturnAttempts = 0;
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
                // Не удалось найти позицию
                if (isReturningToSettlement) {
                    failedReturnAttempts++;
                    // Если много неудач подряд, телепортируем
                    if (failedReturnAttempts > 3) {
                        teleportToSettlement();
                    }
                }
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

    /**
     * Телепортирует бобритто обратно в поселение в случае, если обычными путями он не может вернуться
     */
    private void teleportToSettlement() {
        BlockPos center = bobrito.getSettlementCenter();
        if (center == null) {
            return;
        }
        
        // Проверка телепортации с задержкой - активируем систему проверки прогресса
        if (!teleportCheckActive) {
            // Первый вызов - запоминаем текущую дистанцию и активируем проверку
            teleportCheckActive = true;
            teleportCheckTimer = TELEPORT_CHECK_DELAY; // 5 секунд ожидания
            lastSettlementDistance = Math.sqrt(center.distSqr(bobrito.blockPosition())); // текущая удаленность в блоках
            return;
        }
        
        // Если проверка активна, но таймер еще не истек, просто обновляем его
        if (teleportCheckTimer > 0) {
            teleportCheckTimer--;
            return;
        }
        
        // Таймер истек, проверяем прогресс
        double currentDistance = Math.sqrt(center.distSqr(bobrito.blockPosition()));
            
        // Проверяем, приблизился ли бобритто на 1 блок или больше
        if (currentDistance <= lastSettlementDistance - 1.0) {
            // Бобритто приближается к поселению, повторяем процесс
            teleportCheckTimer = TELEPORT_CHECK_DELAY;
            lastSettlementDistance = currentDistance;
            return;
        }
        
        // Если бобритто не приближается к поселению, телепортируем
        
        // Находим безопасную позицию для телепортации
        BlockPos safePos = findSafePosition(center);
        
        // Телепортируем бобритто
        bobrito.teleportTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
        
        // Сбрасываем счетчики и флаги
        this.isReturningToSettlement = false;
        this.failedReturnAttempts = 0;
        this.teleportCheckActive = false;
        this.teleportCheckTimer = 0;
    }
    
    /**
     * Находит безопасную позицию рядом с центром поселения
     */
    private BlockPos findSafePosition(BlockPos center) {
        // Ищем позицию в радиусе 10 блоков от центра
        int radius = 25;
        BlockPos safePos = center;
        
        // Пробуем найти безопасное место
        for (int attempt = 0; attempt < 25; attempt++) {
            // Генерируем случайное смещение в пределах радиуса
            int offsetX = bobrito.getRandom().nextInt(radius * 2) - radius;
            int offsetZ = bobrito.getRandom().nextInt(radius * 2) - radius;
            
            BlockPos testPos = center.offset(offsetX, 0, offsetZ);
            
            // Находим верхний твердый блок
            BlockPos groundPos = bobrito.level().getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 
                testPos
            );
            
            // Проверяем, что это безопасная позиция (нет лавы и т.д.)
            if (bobrito.level().getBlockState(groundPos.below()).isSolid() && 
                bobrito.level().getBlockState(groundPos).isAir() && 
                bobrito.level().getBlockState(groundPos.above()).isAir()) {
                safePos = groundPos;
                break;
            }
        }
        
        return safePos;
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        // Если нет центра поселения, используем обычную логику блуждания
        if (bobrito.getSettlementCenter() == null) {
            return super.getPosition();
        }
        
        // Если мы слишком далеко от поселения, возвращаемся к центру
        if (isReturningToSettlement || isTooFarFromSettlement()) {
            BlockPos center = bobrito.getSettlementCenter();
            Vec3 centerVec = Vec3.atBottomCenterOf(center);
            
            // Направление к центру поселения
            Vec3 direction = centerVec.subtract(bobrito.position()).normalize();
            
            // Увеличиваем радиус поиска для возвращения в поселение
            int searchRadius = MAX_DISTANCE_FROM_CENTER / 2;
            
            // Генерируем точку в направлении центра поселения
            Vec3 pos = LandRandomPos.getPosTowards(
                this.mob,
                searchRadius,
                7, // Увеличиваем высоту поиска
                direction
            );
            
            // Если не удалось найти путь, используем прямое направление к центру
            if (pos == null) {
                // С каждой неудачной попыткой увеличиваем шанс телепортации
                failedReturnAttempts++;
                
                if (failedReturnAttempts > 3 && bobrito.getRandom().nextInt(3) == 0) {
                    // Телепортируем обратно
                    teleportToSettlement();
                    return null;
                }
                
                // Пробуем найти любую возможную позицию в направлении центра
                pos = DefaultRandomPos.getPosTowards(
                    this.mob,
                    searchRadius,
                    0, // Не ограничиваем вертикальный поиск
                    direction,
                    0.7 // Высокая точность следования направлению
                );
            } else {
                // Сбрасываем счетчик неудачных попыток, если нашли путь
                failedReturnAttempts = 0;
            }
            
            return pos;
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
                // Если это новое обнаружение, начинаем процесс возвращения
                if (!isReturningToSettlement) {
                    // Сбрасываем текущий путь и заставляем сущность найти новый путь к центру
                    this.stop();
                    this.forceTrigger = true;
                    this.isReturningToSettlement = true;
                    
                    // Увеличиваем скорость передвижения при возвращении
                    bobrito.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                        .setBaseValue(0.35D); // Увеличиваем скорость на 40%
                    
                    // Логируем только первое обнаружение для этого бобритто
                    System.out.println("Bobrito left settlement area, returning to center: " + 
                        bobrito.getUUID().toString().substring(0, 8));
                } else {
                    // Если долго не можем вернуться, увеличиваем счетчик неудач
                    failedReturnAttempts++;
                    
                    // Если много неудачных попыток, телепортируем
                    if (failedReturnAttempts > 15) { // Примерно 15 секунд (15 тиков)
                        teleportToSettlement();
                    }
                }
            } else if (isReturningToSettlement) {
                // Если мы успешно вернулись в поселение
                isReturningToSettlement = false;
                failedReturnAttempts = 0;
                
                // Возвращаем нормальную скорость
                bobrito.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                    .setBaseValue(0.25D);
                
                System.out.println("Bobrito returned to settlement: " + 
                    bobrito.getUUID().toString().substring(0, 8));
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
        
        // Если бобритто слишком далеко и требуется телепортация, прекращаем текущее перемещение
        if (isReturningToSettlement && failedReturnAttempts > 10) {
            return false;
        }
        
        // Обычная проверка родительского класса
        return super.canContinueToUse();
    }
    
    @Override
    public void stop() {
        super.stop();
        
        // Если мы прекращаем движение, но все еще возвращаемся, 
        // проверяем, не нужно ли нам телепортироваться
        if (isReturningToSettlement && isTooFarFromSettlement()) {
            failedReturnAttempts++;
            
            // Если много попыток безуспешны, телепортируем
            if (failedReturnAttempts > 5) {
                teleportToSettlement();
            }
        }
    }
}