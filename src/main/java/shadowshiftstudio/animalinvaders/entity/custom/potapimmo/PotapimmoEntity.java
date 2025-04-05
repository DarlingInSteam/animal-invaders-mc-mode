package shadowshiftstudio.animalinvaders.entity.custom.potapimmo;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import java.util.UUID;
import shadowshiftstudio.animalinvaders.entity.ai.potapimmo.PotapimmoAttack;
import shadowshiftstudio.animalinvaders.entity.utils.EntityUtils;
import shadowshiftstudio.animalinvaders.entity.goal.PotapimmoTargetDeadAnimalGoal;

public class PotapimmoEntity extends Monster implements NeutralMob {
    public PotapimmoEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(PotapimmoEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = 
            SynchedEntityData.defineId(PotapimmoEntity.class, EntityDataSerializers.INT);

    // Константа для времени злости - требуется для NeutralMob
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    private final int[] timeoutAnimationState = new int[1];
    public int attackingAnimationsTimeout = 0;

    private UUID persistentAngerTarget;
    private int playerAngerTime;

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            this.updatePersistentAnger((ServerLevel)this.level(), true);
        }

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
        this.entityData.define(DATA_REMAINING_ANGER_TIME, 0);
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
        this.targetSelector.addGoal(2, new PotapimmoTargetDeadAnimalGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true, 
            (entity) -> !(entity instanceof PotapimmoEntity) && !(entity instanceof net.minecraft.world.entity.monster.Creeper)));
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

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, time);
    }

    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(UUID target) {
        this.persistentAngerTarget = target;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.addPersistentAngerSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.readPersistentAngerSaveData(this.level(), tag);
    }

    public boolean canSeePlayer(Player player) {
        return this.distanceTo(player) <= 16.0D && this.getSensing().hasLineOfSight(player);
    }

    public void angerAtPlayer(Player player) {
        if (this.canSeePlayer(player)) {
            this.setTarget(player);
            this.setRemainingPersistentAngerTime(400); // Злится в течение 20 секунд
            this.setPersistentAngerTarget(player.getUUID());
        }
    }
}
