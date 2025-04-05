package shadowshiftstudio.animalinvaders.entity.ai.bobrittobandito;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito.BobrittoBanditoEntity;
import shadowshiftstudio.animalinvaders.entity.custom.bullet.BulletEntity;
import shadowshiftstudio.animalinvaders.entity.ModEntities;

import java.util.EnumSet;

public class BobrittoBanditoAttack extends Goal {
    private final BobrittoBanditoEntity mob;
    private final double speedModifier;
    private int attackIntervalMin;
    private final float attackRadius;
    private final float attackRadiusSqr;
    private int attackTime = -1;
    private int burstCount = 0;
    private int burstCooldown = 0;
    private int maxBurstSize = 8; // Увеличено количество выстрелов в очереди с 3 до 8
    private int burstInterval = 6; // Интервал между выстрелами в очереди (увеличен для распределения 8 выстрелов за 60 тиков)
    private int shootingPreparationTime = 10; // Время до начала стрельбы (подготовка)
    private int shootingAnimationTime = 60; // Длительность анимации стрельбы
    private int reloadTime = 20; // Время перезарядки после стрельбы
    private int attackPhase = 0; // 0 - подготовка, 1 - стрельба, 2 - перезарядка
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public BobrittoBanditoAttack(BobrittoBanditoEntity entity, double speedModifier, int attackIntervalMin, float attackRadius) {
        this.mob = entity;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadius = attackRadius;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean canUse() {
        return this.mob.getTarget() != null;
    }

    public boolean canContinueToUse() {
        return (this.canUse() || !this.mob.getNavigation().isDone());
    }

    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }

    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.mob.setShooting(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.burstCount = 0;
        this.burstCooldown = 0;
        this.attackPhase = 0;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            // Сбрасываем состояние атаки, если цель не существует или мертва
            this.mob.setShooting(false);
            attackTime = -1;
            burstCount = 0;
            burstCooldown = 0;
            attackPhase = 0;
            // Очищаем цель
            this.mob.setTarget(null);
            return;
        }

        double distanceToTargetSq = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean canSeeTarget = this.mob.getSensing().hasLineOfSight(target);

        if (canSeeTarget) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        // Логика передвижения и стрельбы
        if (distanceToTargetSq <= (double)this.attackRadiusSqr && this.seeTime >= 5) {
            this.mob.getNavigation().stop();
            ++this.strafingTime;
        } else {
            this.mob.getNavigation().moveTo(target, this.speedModifier);
            this.strafingTime = -1;
        }

        if (this.strafingTime >= 20) {
            if ((double)this.mob.getRandom().nextFloat() < 0.3D) {
                this.strafingClockwise = !this.strafingClockwise;
            }

            if ((double)this.mob.getRandom().nextFloat() < 0.3D) {
                this.strafingBackwards = !this.strafingBackwards;
            }

            this.strafingTime = 0;
        }

        // Управление стрейфингом
        if (this.strafingTime > -1) {
            if (distanceToTargetSq > (double)(this.attackRadiusSqr * 0.75F)) {
                this.strafingBackwards = false;
            } else if (distanceToTargetSq < (double)(this.attackRadiusSqr * 0.25F)) {
                this.strafingBackwards = true;
            }

            this.mob.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
            this.mob.lookAt(target, 30.0F, 30.0F);
        } else {
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        // Обновленная логика стрельбы с учетом фаз атаки
        if (burstCooldown > 0) {
            burstCooldown--;
            return;
        }

        // Проверка наличия цели и дистанции
        if (canSeeTarget && (distanceToTargetSq <= (double)this.attackRadiusSqr)) {
            // Если атака не начата и нет активного кулдауна - начинаем новую атаку
            if (attackTime == -1) {
                this.attackTime = 0;
                this.attackPhase = 0; // Начинаем с фазы подготовки
                this.burstCount = 0;
                // На этом этапе не включаем анимацию стрельбы
            }
            
            if (attackTime >= 0) {
                attackTime++;
                
                // Фаза подготовки (10 тиков)
                if (attackPhase == 0) {
                    if (attackTime >= shootingPreparationTime) {
                        attackPhase = 1; // Переходим в фазу стрельбы
                        this.mob.setShooting(true); // Включаем анимацию стрельбы
                        attackTime = 0; // Сбрасываем счетчик для фазы стрельбы
                    }
                } 
                // Фаза стрельбы (60 тиков)
                else if (attackPhase == 1) {
                    // Распределение 8 выстрелов на 60 тиков (примерно каждые 7-8 тиков)
                    if (attackTime % burstInterval == 0 && burstCount < maxBurstSize) {
                        performShot(target);
                        burstCount++;
                    }
                    
                    // Переход к перезарядке после завершения 60 тиков стрельбы
                    if (attackTime >= shootingAnimationTime) {
                        attackPhase = 2; // Переходим в фазу перезарядки
                        // Важно! НЕ выключаем анимацию стрельбы здесь, чтобы прошла анимация перезарядки
                        attackTime = 0; // Сбрасываем счетчик для фазы перезарядки
                    }
                } 
                // Фаза перезарядки (20 тиков)
                else if (attackPhase == 2) {
                    if (attackTime >= reloadTime) {
                        // Завершаем цикл атаки и выключаем анимацию
                        attackTime = -1;
                        this.mob.setShooting(false); // Выключаем анимацию только после завершения перезарядки
                        burstCooldown = 10; // Добавляем небольшой кулдаун между циклами атаки
                    }
                }
            }
        } else {
            // Если цель потеряна или слишком далеко - сбрасываем атаку
            if (this.mob.isShooting()) {
                this.mob.setShooting(false);
            }
            attackTime = -1;
            attackPhase = 0;
        }
    }

    private void performShot(LivingEntity target) {
        if (!this.mob.level().isClientSide) {
            // Создаем новую пулю вместо стрелы
            BulletEntity bullet = new BulletEntity(ModEntities.BULLET.get(), this.mob, this.mob.level());
            
            // Настройка свойств пули
            bullet.setNoGravity(false); // Пуля подчиняется гравитации
            bullet.setBaseDamage(2.5); // Установка базового урона
            bullet.setPierceLevel((byte)0); // Пуля не пробивает насквозь и не рикошетит
            bullet.setCritArrow(false); // Не критическая атака
            bullet.setKnockback(0); // Минимальный отброс
            bullet.setSoundEvent(SoundEvents.COPPER_BREAK); // Звук при попадании - звук ломающейся меди
            
            // Делаем пулю исчезающей после столкновения
            bullet.setShotFromCrossbow(true); 
            bullet.pickup = AbstractArrow.Pickup.DISALLOWED; // Пулю нельзя подобрать

            // Добавляем небольшой разброс для автоматической стрельбы
            double spreadFactor = 0.2; // Уменьшен разброс для более точной стрельбы
            double xSpread = (this.mob.getRandom().nextDouble() - 0.5) * spreadFactor;
            double ySpread = (this.mob.getRandom().nextDouble() - 0.5) * spreadFactor * 0.5; // Уменьшаем вертикальный разброс
            double zSpread = (this.mob.getRandom().nextDouble() - 0.5) * spreadFactor;
            
            // Получаем позицию цели
            double d0 = target.getX() - this.mob.getX();
            double d1 = target.getY(0.3333333333333333D) - bullet.getY();
            double d2 = target.getZ() - this.mob.getZ();
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            
            // Запускаем пулю с учетом разброса
            bullet.shoot(
                d0 + xSpread,
                d1 + d3 * 0.20000000298023224D + ySpread,
                d2 + zSpread,
                1.6F,
                (float)(14 - this.mob.level().getDifficulty().getId() * 4)
            );
            
            // Звук выстрела - используем более подходящий звук
            this.mob.playSound(SoundEvents.BLASTFURNACE_FIRE_CRACKLE, 1.0F, 1.8F / (this.mob.getRandom().nextFloat() * 0.4F + 0.8F));
            this.mob.level().addFreshEntity(bullet);
        }
    }
}