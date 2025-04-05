package shadowshiftstudio.animalinvaders.entity.custom.potapimmo;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import shadowshiftstudio.animalinvaders.entity.ai.potapimmo.PotapimmoAttack;
import shadowshiftstudio.animalinvaders.entity.utils.EntityUtils;

public class PotapimmoEntity extends Monster {
    public PotapimmoEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(PotapimmoEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    private final int[] timeoutAnimationState = new int[1];
    public int attackingAnimationsTimeout = 0;

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            // Используем утилитный метод для управления анимацией простоя
            EntityUtils.setupIdleAnimation(idleAnimationState, this.tickCount, timeoutAnimationState);
        }

        if (this.isAttacking() && attackingAnimationsTimeout <= 0) {
            attackingAnimationsTimeout = 60;
            // Используем утилитный метод для переключения анимаций
            EntityUtils.switchAnimation(idleAnimationState, attackAnimationState, this.tickCount);
        } else {
            // Используем утилитный метод для обработки таймаута
            attackingAnimationsTimeout = EntityUtils.handleAnimationTimeout(attackingAnimationsTimeout, null);
        }

        if (!this.isAttacking()) {
            attackAnimationState.stop();
        }
    }

    @Override
    protected void updateWalkAnimation(float partialTicks) {
        // Используем утилитный метод для обновления анимации ходьбы
        EntityUtils.updateWalkAnimation(this, partialTicks);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);

        if (attacking) {
            this.idleAnimationState.stop();
        }
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PotapimmoAttack(this, 1.0D, true));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 32.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true, 
            (entity) -> !(entity instanceof PotapimmoEntity)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ATTACK_SPEED, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.ARMOR, 3.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 5.0D);
    }
}
