package shadowshiftstudio.animalinvaders.entity.ai.tralalerotralala;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import shadowshiftstudio.animalinvaders.entity.custom.tralalerotralala.TralaleroTralalaEntity;

public class TralaleroTralalaAttack extends MeleeAttackGoal {
    private final TralaleroTralalaEntity tralalaEntity;
    private int attackDelay = 20; // Общая задержка между атаками в тиках
    private int ticksUntilNextAttack = 20;

    public TralaleroTralalaAttack(PathfinderMob mob, double speed, boolean pauseWhenMobIdle) {
        super(mob, speed, pauseWhenMobIdle);
        tralalaEntity = ((TralaleroTralalaEntity) mob);
    }

    protected double getAttackReachSqr(LivingEntity entity) {
        return (double)(this.mob.getBbWidth() * 4.0F * this.mob.getBbWidth() * 4.0F + entity.getBbWidth());
    }

    private boolean isEnemyWithinAttackDistance(LivingEntity pEnemy, double pDistToEnemySqr) {
        return pDistToEnemySqr <= this.getAttackReachSqr(pEnemy);
    }

    protected void resetAttackCooldown() {
        // Увеличиваем задержку между атаками для более естественного поведения
        this.ticksUntilNextAttack = this.adjustedTickDelay(attackDelay * 2); // ~40 тиков (2 секунды)
    }

    protected boolean isTimeToAttack() {
        return this.ticksUntilNextAttack <= 0;
    }

    protected int getTicksUntilNextAttack() {
        return this.ticksUntilNextAttack;
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity pEnemy, double pDistToEnemySqr) {
        if (isEnemyWithinAttackDistance(pEnemy, pDistToEnemySqr)) {
            // Если враг в пределах досягаемости и пора атаковать
            if (isTimeToAttack()) {
                tralalaEntity.setAttacking(true);
                // Обновляем направление взгляда для более точной атаки
                this.mob.getLookControl().setLookAt(pEnemy.getX(), pEnemy.getEyeY(), pEnemy.getZ());
                
                // Вызываем doHurtTarget у TralaleroTralalaEntity, там включается анимация
                // и настраивается отложенное нанесение урона в определенный момент анимации
                this.mob.doHurtTarget(pEnemy);
                
                // Сбрасываем кулдаун атаки
                resetAttackCooldown();
            }
        } else {
            // Если враг вышел из диапазона атаки
            if (!tralalaEntity.isAttacking()) {
                // Сбрасываем таймер атаки только если не в процессе анимации атаки
                // Это предотвращает прерывание текущей анимации атаки
                resetAttackCooldown();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        
        // Уменьшаем счетчик до следующей атаки
        if (this.ticksUntilNextAttack > 0) {
            --this.ticksUntilNextAttack;
        }
    }

    @Override
    public void stop() {
        // Нет необходимости здесь останавливать анимацию атаки, 
        // так как это обрабатывается в самом TralaleroTralalaEntity
        // с учетом таймаутов для плавного завершения анимации
        super.stop();
    }
}