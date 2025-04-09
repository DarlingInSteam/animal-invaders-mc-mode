package shadowshiftstudio.animalinvaders.entity.custom.lirililarila;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import shadowshiftstudio.animalinvaders.entity.utils.EntityUtils;

import javax.annotation.Nullable;
import java.util.UUID;

public class LiriliLarilaEntity extends Animal {
    // Animation states
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState hideInAnimationState = new AnimationState();
    public final AnimationState hideOutAnimationState = new AnimationState();
    
    // Animation timeouts
    private int idleTimeout = 0;
    private int hideInTimeout = 0;
    private int hideOutTimeout = 0;
    
    // Synced data
    private static final EntityDataAccessor<Boolean> HIDING = 
            SynchedEntityData.defineId(LiriliLarilaEntity.class, EntityDataSerializers.BOOLEAN);
    
    // For damage reflection
    @Nullable
    private UUID lastAttackerUUID;
    private int lastAttackerCheckTimer = 0;
    private static final int ATTACKER_CHECK_FREQUENCY = 10; // Check every 10 ticks (0.5 seconds)
    private static final double SAFE_DISTANCE_SQUARED = 16 * 16; // 16 blocks squared
    
    public LiriliLarilaEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HIDING, false);
    }
    
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Удаляем PanicGoal, так как нам не нужно, чтобы моб убегал при получении урона
        // this.goalSelector.addGoal(1, new PanicGoal(this, 1.25D));
        
        // Добавляем AvoidEntityGoal только когда не в состоянии hiding
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.2D, 1.5D) {
            @Override
            public boolean canUse() {
                return !LiriliLarilaEntity.this.isHiding() && super.canUse();
            }
            
            @Override
            public boolean canContinueToUse() {
                return !LiriliLarilaEntity.this.isHiding() && super.canContinueToUse();
            }
        });
        
        // Другие цели должны работать только когда моб не прячется
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return !LiriliLarilaEntity.this.isHiding() && super.canUse();
            }
            
            @Override
            public boolean canContinueToUse() {
                return !LiriliLarilaEntity.this.isHiding() && super.canContinueToUse();
            }
        });
        
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F) {
            @Override
            public boolean canUse() {
                return !LiriliLarilaEntity.this.isHiding() && super.canUse();
            }
            
            @Override
            public boolean canContinueToUse() {
                return !LiriliLarilaEntity.this.isHiding() && super.canContinueToUse();
            }
        });
        
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !LiriliLarilaEntity.this.isHiding() && super.canUse();
            }
            
            @Override
            public boolean canContinueToUse() {
                return !LiriliLarilaEntity.this.isHiding() && super.canContinueToUse();
            }
        });
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // Если моб прячется, остановим его движение
        if (this.isHiding() && !this.level().isClientSide()) {
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        }
        
        if (this.level().isClientSide()) {
            // Handle animations
            if (this.isHiding()) {
                if (hideInTimeout <= 0) {
                    hideInAnimationState.startIfStopped(this.tickCount);
                    hideInTimeout = 20; // Animation duration in ticks
                } else {
                    hideInTimeout--;
                }
                
                // Когда прячемся, останавливаем другие анимации
                walkAnimationState.stop();
                idleAnimationState.stop();
            } else {
                hideInAnimationState.stop();
                
                if (hideOutTimeout > 0) {
                    hideOutAnimationState.startIfStopped(this.tickCount);
                    hideOutTimeout--;
                    
                    // Когда выходим из прятания, также останавливаем другие анимации
                    walkAnimationState.stop();
                    idleAnimationState.stop();
                } else {
                    hideOutAnimationState.stop();
                    
                    // Normal animations when not hiding
                    if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6) {
                        // Moving - play walk animation
                        walkAnimationState.startIfStopped(this.tickCount);
                        idleAnimationState.stop();
                    } else {
                        // Not moving - play idle animation
                        idleAnimationState.startIfStopped(this.tickCount);
                        walkAnimationState.stop();
                    }
                }
            }
        }
        
        // Only run on server side
        if (!this.level().isClientSide() && this.isHiding() && this.lastAttackerUUID != null) {
            // Check the attacker's distance periodically
            lastAttackerCheckTimer++;
            if (lastAttackerCheckTimer >= ATTACKER_CHECK_FREQUENCY) {
                lastAttackerCheckTimer = 0;
                checkAttackerDistance();
            }
        }
    }
    
    private void checkAttackerDistance() {
        if (this.lastAttackerUUID == null) return;
        
        // Find the attacker entity by UUID
        Entity attacker = findEntityByUUID(this.lastAttackerUUID);
        if (attacker != null) {
            // Check distance
            double distanceSquared = this.distanceToSqr(attacker);
            if (distanceSquared > SAFE_DISTANCE_SQUARED) {
                // Attacker is far enough away, exit hiding
                setHiding(false);
                this.lastAttackerUUID = null;
                this.hideOutTimeout = 10; // Start hide_out animation
            }
        } else {
            // If attacker can't be found, also exit hiding
            setHiding(false);
            this.lastAttackerUUID = null;
            this.hideOutTimeout = 10; // Start hide_out animation
        }
    }
    
    @Nullable
    private Entity findEntityByUUID(UUID uuid) {
        for (Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate(SAFE_DISTANCE_SQUARED))) {
            if (entity.getUUID().equals(uuid)) {
                return entity;
            }
        }
        return null;
    }
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Если моб уже прячется, он должен отражать весь урон
        if (this.isHiding()) {
            // When hiding, reflect damage back to attacker
            Entity attacker = source.getEntity();
            if (attacker instanceof LivingEntity) {
                attacker.hurt(this.damageSources().thorns(this), amount * 1.5f);  // Увеличиваем отраженный урон на 50%
                // Можно добавить эффект звука или частиц для обратного урона
                // this.playSound(SoundEvents.THORNS_HIT, 1.0F, 1.0F);
            }
            return false; // Prevent damage to self
        }
        
        // Not hiding yet, take damage and start hiding
        boolean wasHurt = super.hurt(source, amount);
        
        if (wasHurt && !this.isDeadOrDying()) {
            // Start hiding
            setHiding(true);
            
            // Сбрасываем скорость, чтобы моб сразу остановился
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
            
            // Remember attacker for distance checks
            if (source.getEntity() != null) {
                this.lastAttackerUUID = source.getEntity().getUUID();
            }
            
            // Запускаем таймер анимации hide_in
            this.hideInTimeout = 20;
        }
        
        return wasHurt;
    }
    
    @Override
    protected void updateWalkAnimation(float partialTicks) {
        EntityUtils.updateWalkAnimation(this, partialTicks);
    }
    
    public boolean isHiding() {
        return this.entityData.get(HIDING);
    }
    
    public void setHiding(boolean hiding) {
        this.entityData.set(HIDING, hiding);
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Hiding", isHiding());
        if (this.lastAttackerUUID != null) {
            tag.putUUID("LastAttacker", this.lastAttackerUUID);
        }
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setHiding(tag.getBoolean("Hiding"));
        if (tag.hasUUID("LastAttacker")) {
            this.lastAttackerUUID = tag.getUUID("LastAttacker");
        }
    }
    
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RABBIT_AMBIENT;
    }
    
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.RABBIT_HURT;
    }
    
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RABBIT_DEATH;
    }
    
    @Override
    public LiriliLarilaEntity getBreedOffspring(net.minecraft.server.level.ServerLevel level, AgeableMob otherParent) {
        return null; // No breeding implemented
    }
}