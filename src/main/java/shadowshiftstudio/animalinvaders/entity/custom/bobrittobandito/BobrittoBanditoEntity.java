package shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import shadowshiftstudio.animalinvaders.entity.ai.bobrittobandito.BobrittoBanditoAttack;
import shadowshiftstudio.animalinvaders.entity.utils.EntityUtils;

public class BobrittoBanditoEntity extends Monster implements RangedAttackMob {
    // Синхронизированные данные
    private static final EntityDataAccessor<Boolean> DATA_IS_SHOOTING =
            SynchedEntityData.defineId(BobrittoBanditoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_RUNNING =
            SynchedEntityData.defineId(BobrittoBanditoEntity.class, EntityDataSerializers.BOOLEAN);

    // Состояния анимаций
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();

    private int shootingTimeout = 0;
    private int runTimeout = 0;
    private int idleTimeout = 0;
    private int shootCooldown = 0;

    public BobrittoBanditoEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_SHOOTING, false);
        this.entityData.define(DATA_IS_RUNNING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BobrittoBanditoAttack(this, 1.5D, 10, 20.0F));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 32.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.ATTACK_SPEED, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR, 5.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0D);
    }

    public boolean isShooting() {
        return this.entityData.get(DATA_IS_SHOOTING);
    }

    public void setShooting(boolean shooting) {
        this.entityData.set(DATA_IS_SHOOTING, shooting);
    }

    public boolean isRunning() {
        return this.entityData.get(DATA_IS_RUNNING);
    }

    public void setRunning(boolean running) {
        this.entityData.set(DATA_IS_RUNNING, running);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            LivingEntity target = getTarget();
            if (target != null && target.isAlive()) {
                double distSq = this.distanceToSqr(target);
                setRunning(distSq <= 256.0D && distSq > 25.0D);

                if (isRunning()) {
                    this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.4D);
                    runTimeout = 5;
                } else {
                    // Используем утилитный метод для обработки таймаута
                    runTimeout = EntityUtils.handleAnimationTimeout(runTimeout, () -> {
                        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.25D);
                    });
                }
                
                // Используем утилитный метод для обработки таймаутов
                shootingTimeout = EntityUtils.handleAnimationTimeout(shootingTimeout, () -> {
                    setShooting(false);
                });
                
                shootCooldown = EntityUtils.handleAnimationTimeout(shootCooldown, null);
            } else {
                // Используем утилитный метод для обработки таймаута
                runTimeout = EntityUtils.handleAnimationTimeout(runTimeout, () -> {
                    setRunning(false);
                    this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.25D);
                });
                
                // Используем утилитный метод для обработки таймаута
                shootingTimeout = EntityUtils.handleAnimationTimeout(shootingTimeout, () -> {
                    setShooting(false);
                });
            }
        }

        if (this.level().isClientSide()) {
            // Анимация стрельбы переопределяет другие анимации, но не останавливает их
            if (this.isShooting()) {
                this.attackAnimationState.startIfStopped(this.tickCount);
            } else {
                this.attackAnimationState.stop();
            }

            // Используем утилитный метод для управления анимациями движения
            EntityUtils.handleMovementAnimations(
                this.isRunning(), 
                this, 
                this.runAnimationState, 
                this.walkAnimationState, 
                this.idleAnimationState, 
                this.tickCount
            );
        }
    }

    @Override
    protected void updateWalkAnimation(float partialTicks) {
        // Используем утилитный метод для обновления анимации ходьбы
        EntityUtils.updateWalkAnimation(this, partialTicks);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (isShooting() || shootCooldown > 0) {
            return;
        }

        setShooting(true);
        shootingTimeout = 110; // Увеличено с 90 до 110 тиков (подготовка + стрельба + увеличенная перезарядка)
        shootCooldown = 20; // 1 секунда кулдауна между выстрелами

        if (!this.level().isClientSide) {
            AbstractArrow arrow = ProjectileUtil.getMobArrow(this, new ItemStack(Items.ARROW), velocity);
            
            // Настройки стрелы: добавляем гравитацию, убираем рикошет, стрела исчезает после столкновения
            arrow.setNoGravity(false); // Включаем гравитацию
            arrow.setPierceLevel((byte)0); // Нет пробивания
            arrow.setSoundEvent(SoundEvents.ARROW_HIT); // Звук при попадании
            arrow.setNoPhysics(false); // Включаем физику
            
            double d0 = target.getX() - this.getX();
            double d1 = target.getY(0.3333333333333333D) - arrow.getY();
            double d2 = target.getZ() - this.getZ();
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);

            arrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F,
                   (float)(14 - this.level().getDifficulty().getId() * 4));

            this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.level().addFreshEntity(arrow);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }
}