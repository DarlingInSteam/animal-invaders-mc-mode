package shadowshiftstudio.animalinvaders.entity.custom.tralalerotralala;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import shadowshiftstudio.animalinvaders.entity.utils.EntityUtils;

import java.util.UUID;

public class TralaleroTralalaEntity extends Monster implements NeutralMob {
    private static final EntityDataAccessor<Boolean> ATTACKING = 
            SynchedEntityData.defineId(TralaleroTralalaEntity.class, EntityDataSerializers.BOOLEAN);
    
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = 
            SynchedEntityData.defineId(TralaleroTralalaEntity.class, EntityDataSerializers.INT);
            
    // Constants for anger time (required for NeutralMob)
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(15, 30);
    
    // Animation states
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    private final int[] timeoutAnimationState = new int[1];
    public int attackingAnimationsTimeout = 0;
    
    // Система контроля атаки
    private int attackTick = 0;
    private Entity targetedEntity = null;
    private static final int DAMAGE_TICK = 13; // Тик, на котором должен быть нанесен урон
    
    private UUID persistentAngerTarget;

    public TralaleroTralalaEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        
        // Инициализация анимационных состояний с указанием того, что мы хотим,
        // чтобы они запускались при создании сущности
        if (level.isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (!this.level().isClientSide()) {
            this.updatePersistentAnger((ServerLevel)this.level(), true);
            
            // Если есть активная атака, отслеживаем тики
            if (this.isAttacking() && targetedEntity != null) {
                attackTick++;
                
                // Наносим урон только в определенный момент анимации
                if (attackTick == DAMAGE_TICK && targetedEntity.isAlive()) {
                    float damage = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    targetedEntity.hurt(this.level().damageSources().mobAttack(this), damage);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), 
                            SoundEvents.PLAYER_ATTACK_STRONG, this.getSoundSource(), 1.0F, 1.0F);
                }
                
                // Сбрасываем механику атаки после её завершения
                if (attackTick >= (int)(1.375 * 20)) {
                    attackTick = 0;
                    targetedEntity = null;
                    this.setAttacking(false);
                }
            }
        }
        
        if (this.level().isClientSide()) {
            // Используем утилитный метод для управления анимацией простоя
            EntityUtils.setupIdleAnimation(idleAnimationState, this.tickCount, timeoutAnimationState);
            
            // Обрабатываем таймаут анимации атаки
            if (attackingAnimationsTimeout > 0) {
                attackingAnimationsTimeout--;
            }
        }
        
        // Обновим анимации в зависимости от состояния
        if (this.level().isClientSide()) {
            this.handleAnimations();
        }
    }
    
    private boolean isMoving() {
        // Более чувствительное определение движения: проверяем не только скорость,
        // но и состояние навигации
        return this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-7D || 
               (this.getNavigation() != null && this.getNavigation().isInProgress());
    }
    
    @Override
    protected void updateWalkAnimation(float partialTicks) {
        float f;
        if (this.isMoving()) {
            f = Math.min(partialTicks * 6.0F, 1.0F);
        } else {
            f = 0.0F;
        }
        
        this.walkAnimation.update(f, 0.2F);
    }
    
    public void setAttacking(boolean attacking) {
        boolean wasAttacking = this.entityData.get(ATTACKING);
        this.entityData.set(ATTACKING, attacking);
        
        if (this.level().isClientSide()) {
            // Если начинаем атаку и раньше не атаковали
            if (attacking && !wasAttacking) {
                // Останавливаем другие анимации
                idleAnimationState.stop();
                walkAnimationState.stop();
                
                // Запускаем анимацию атаки с самого начала (важно для синхронизации)
                attackAnimationState.stop();  // Сначала останавливаем, чтобы точно начать с начала
                attackAnimationState.start(this.tickCount);
                
                // Устанавливаем таймаут на полную длительность анимации атаки
                attackingAnimationsTimeout = (int)(1.375 * 20);
            }
            // Если заканчиваем атаку и раньше атаковали
            else if (!attacking && wasAttacking) {
                // Не останавливаем анимацию атаки сразу, пусть таймаут её завершит
                // Это позволит анимации атаки доиграть до конца
                if (attackingAnimationsTimeout <= 0) {
                    attackAnimationState.stop();
                }
            }
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
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new shadowshiftstudio.animalinvaders.entity.ai.tralalerotralala.TralaleroTralalaAttack(this, 1.0D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D)); // Slightly slower movement
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        
        // Target selector goals for aggression
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 25.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D) // 20% slower than player (player speed is 0.25)
                .add(Attributes.ARMOR, 2.0D);
    }
    
    // Sound methods
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.TROPICAL_FISH_AMBIENT;
    }
    
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.TROPICAL_FISH_HURT;
    }
    
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.TROPICAL_FISH_DEATH;
    }

    // Required methods for NeutralMob interface
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
    
    // Static method for spawn rules
    public static boolean checkTralaleroTralalaSpawnRules(EntityType<TralaleroTralalaEntity> entityType, 
                                                 LevelAccessor level,
                                                 MobSpawnType spawnType, 
                                                 BlockPos pos, 
                                                 RandomSource random) {
        // Check if biome is a beach - only spawn on beaches
        if (level.getBiome(pos).is(BiomeTags.IS_BEACH)) {
            // Make sure there's a solid block below and it's at the right height
            return level.getBlockState(pos.below()).isValidSpawn(level, pos.below(), entityType) &&
                   level.getRawBrightness(pos, 0) > 8;
        }
        return false;
    }
    
    @Override
    public boolean doHurtTarget(Entity target) {
        // Начинаем анимацию атаки
        this.setAttacking(true);
        
        // Сохраняем цель для последующего нанесения урона
        this.targetedEntity = target;
        this.attackTick = 0;
        
        return true; // Всегда возвращаем true, поскольку урон будет нанесен позже
    }

    /**
     * Управляет анимациями сущности в зависимости от текущего состояния
     */
    private void handleAnimations() {
        if (this.level().isClientSide()) {
            // Check if we're still in the attack animation timeout period
            if (attackingAnimationsTimeout > 0) {
                // Keep the attack animation going, suppress other animations
                walkAnimationState.stop();
                idleAnimationState.stop();
                attackAnimationState.start(this.tickCount);
            } else if (this.isMoving()) {
                // If not attacking and moving
                attackAnimationState.stop();
                idleAnimationState.stop();
                walkAnimationState.startIfStopped(this.tickCount);
            } else {
                // If not attacking and not moving
                attackAnimationState.stop();
                walkAnimationState.stop();
                idleAnimationState.startIfStopped(this.tickCount);
            }
        }
    }
    
    @Override
    public void aiStep() {
        super.aiStep();
        
        // Обновляем анимации на каждом тике ИИ
        if (this.level().isClientSide()) {
            this.handleAnimations();
        }
    }
}