package shadowshiftstudio.animalinvaders.entity.ai;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import shadowshiftstudio.animalinvaders.entity.custom.PotapimmoEntity;

public class PotapimmoAttack extends MeleeAttackGoal {
    private final PotapimmoEntity potapimmoEntity;
    private int attackDelay = 30;
    private int ticksUntilNextAttack = 20;
    private boolean shouldCountTillNextAttack = false;

    public PotapimmoAttack(PathfinderMob p_25552_, double p_25553_, boolean p_25554_) {
        super(p_25552_, p_25553_, p_25554_);

        potapimmoEntity = ((PotapimmoEntity) p_25552_);
    }

    protected double getAttackReachSqr(LivingEntity entity) {
        return (double)(this.mob.getBbWidth() * 5.0F * this.mob.getBbWidth() * 5.0F + entity.getBbWidth());
    }

    private boolean isEnemyWithinAttackDistance(LivingEntity pEnemy, double pDistToEnemySqr) {
        return pDistToEnemySqr <= this.getAttackReachSqr(pEnemy);
    }

    protected void resetAttackCooldown() {
        this.ticksUntilNextAttack = this.adjustedTickDelay(attackDelay * 2);
    }

    protected boolean isTimeToAttack() {
        return this.ticksUntilNextAttack <= 0;
    }

    protected boolean isTimeToStartAttackAnimation() {
        return this.ticksUntilNextAttack <= attackDelay;
    }

    protected int getTicksUntilNextAttack() {
        return this.ticksUntilNextAttack;
    }

    protected void performAttack(LivingEntity pTarget) {
        this.resetAttackCooldown();
        this.mob.swing(InteractionHand.MAIN_HAND);
        boolean attackSuccess = this.mob.doHurtTarget(pTarget);

        if (attackSuccess) {
            double x = pTarget.getDeltaMovement().x;
            double y = pTarget.getDeltaMovement().y;
            double z = pTarget.getDeltaMovement().z;

            pTarget.setDeltaMovement(x, y + 0.7D, z);

            pTarget.hurtMarked = true;
        }
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity pEnemy, double pDistToEnemySqr) {
        if (isEnemyWithinAttackDistance(pEnemy, pDistToEnemySqr)) {
            shouldCountTillNextAttack = true;

            if(isTimeToStartAttackAnimation()) {
                potapimmoEntity.setAttacking(true);
            }

            if(isTimeToAttack()) {
                this.mob.getLookControl().setLookAt(pEnemy.getX(), pEnemy.getEyeY(), pEnemy.getZ());
                performAttack(pEnemy);
            }
        } else {
            resetAttackCooldown();
            shouldCountTillNextAttack = false;
            potapimmoEntity.setAttacking(false);
            potapimmoEntity.attackingAnimationsTimeout = 0;
        }
    }

    @Override
    public void start() {
        super.start();

        attackDelay = 30;
        ticksUntilNextAttack = 20;
    }

    @Override
    public void tick() {
        super.tick();

        if (shouldCountTillNextAttack) {
            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
        }
    }

    @Override
    public void stop() {
        potapimmoEntity.setAttacking(false);

        super.stop();
    }
}
