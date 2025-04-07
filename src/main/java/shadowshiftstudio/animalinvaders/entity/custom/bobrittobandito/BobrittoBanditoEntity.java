package shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import shadowshiftstudio.animalinvaders.block.settlement.BobrittoManager;
import shadowshiftstudio.animalinvaders.block.settlement.SettlementManager;
import shadowshiftstudio.animalinvaders.entity.ai.bobrittobandito.BobrittoFollowLeaderGoal;
import shadowshiftstudio.animalinvaders.entity.ai.bobrittobandito.BobrittoPatrolGoal;
import shadowshiftstudio.animalinvaders.entity.ai.bobrittobandito.BobrittoSettlementWanderGoal;
import shadowshiftstudio.animalinvaders.entity.ai.bobrittobandito.BobrittoBanditoAttack;
import shadowshiftstudio.animalinvaders.entity.utils.EntityUtils;
import shadowshiftstudio.animalinvaders.entity.custom.potapimmo.PotapimmoEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;

import java.util.UUID;

public class BobrittoBanditoEntity extends Monster implements RangedAttackMob {
    // Синхронизированные данные
    private static final EntityDataAccessor<Boolean> DATA_IS_SHOOTING =
            SynchedEntityData.defineId(BobrittoBanditoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_RUNNING =
            SynchedEntityData.defineId(BobrittoBanditoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_PATROL_LEADER =
            SynchedEntityData.defineId(BobrittoBanditoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_PATROL_FOLLOWER =
            SynchedEntityData.defineId(BobrittoBanditoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_PATROL_STATE =
            SynchedEntityData.defineId(BobrittoBanditoEntity.class, EntityDataSerializers.INT);

    // Состояния анимаций
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();

    private int shootingTimeout = 0;
    private int runTimeout = 0;
    private int idleTimeout = 0;
    private int shootCooldown = 0;
    
    // Данные для патрулирования
    private UUID leaderUUID;
    private Vec3[] patrolPoints;
    private int currentPatrolPoint = 0;
    private boolean completedOuterPatrol = false;
    private int registerWithSettlementTimer = 20; // Регистрация с поселением через секунду после спавна
    private BlockPos settlementCenter;

    public BobrittoBanditoEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_SHOOTING, false);
        this.entityData.define(DATA_IS_RUNNING, false);
        this.entityData.define(DATA_IS_PATROL_LEADER, false);
        this.entityData.define(DATA_IS_PATROL_FOLLOWER, false);
        this.entityData.define(DATA_PATROL_STATE, BobrittoManager.PatrolState.PATROLLING_OUTER.ordinal());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        
        // Патрульные цели - высокий приоритет, но ниже, чем атака
        this.goalSelector.addGoal(1, new BobrittoPatrolGoal(this, 1.0D));
        this.goalSelector.addGoal(1, new BobrittoFollowLeaderGoal(this, 1.0D, 2.0F, 10.0F));
        
        this.goalSelector.addGoal(2, new BobrittoBanditoAttack(this, 1.5D, 10, 20.0F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, false));
        
        // Свободное блуждание по поселению для обычных бобритто
        this.goalSelector.addGoal(4, new BobrittoSettlementWanderGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 32.0F));

        // Модифицируем цель HurtByTarget, чтобы исключить бобритто-бандито из возможных целей
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canContinueToUse() {
                // Проверяем, что цель не бобритто-бандито
                if (this.mob.getTarget() instanceof BobrittoBanditoEntity) {
                    return false;
                }
                // Проверяем, что цель не животное
                if (this.mob.getTarget() instanceof net.minecraft.world.entity.animal.Animal) {
                    return false;
                }
                return super.canContinueToUse();
            }
            
            @Override
            protected void alertOther(Mob mob, LivingEntity target) {
                // Не оповещаем о целях, если цель - бобритто-бандито или животное
                if (target instanceof BobrittoBanditoEntity || 
                    target instanceof net.minecraft.world.entity.animal.Animal) {
                    return;
                }
                super.alertOther(mob, target);
            }
        }.setAlertOthers());
        
        // Атака игроков - высокий приоритет
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        
        // Атака на потапиммо - такой же высокий приоритет
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PotapimmoEntity.class, true));
        
        // Нападаем на жителей
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Villager.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, WanderingTrader.class, true));
        
        // Атака железных големов
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        
        // Атака других мобов, исключая животных
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, (mob) -> {
            // Исключаем животных из целей
            if (mob instanceof net.minecraft.world.entity.animal.Animal) {
                return false;
            }
            
            // Атакуем разумные существа, которые не являются монстрами и не являются бобритто
            return mob instanceof AgeableMob && 
                   !(mob instanceof BobrittoBanditoEntity) && 
                   mob.getMobType() != MobType.UNDEAD &&
                   mob.getMobType() != MobType.ARTHROPOD;
        }));
        
        // Сохраняем атаку на скелетов и разбойников
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.Skeleton.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.Pillager.class, true));
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
            // Регистрация с поселением после спавна
            if (registerWithSettlementTimer > 0) {
                registerWithSettlementTimer--;
                if (registerWithSettlementTimer <= 0) {
                    registerWithSettlement();
                }
            }
            
            // Обновление статуса патруля для лидера
            if (isPatrolLeader()) {
                if (patrolPoints == null) {
                    generatePatrolPath();
                }
                
                // Сброс флага завершения патруля, если начинаем патрулировать заново
                if (getPatrolState() == BobrittoManager.PatrolState.PATROLLING_OUTER && completedOuterPatrol) {
                    completedOuterPatrol = false;
                }
            }

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
            
            // Помечаем стрелу как выпущенную бобритто-бандито, чтобы другие бобритто-бандито не получали урон
            arrow.setOwner(this);
            
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
    public boolean hurt(DamageSource source, float amount) {
        // Проверяем, не наносит ли урон другой бобритто
        Entity attacker = source.getEntity();
        
        // Если атакует другой бобритто - отменяем урон
        if (attacker instanceof BobrittoBanditoEntity) {
            return false;
        }
        
        // Вызываем базовый метод для обработки урона
        boolean wasHurt = super.hurt(source, amount);
        
        // Если был нанесен урон и мы на сервере
        if (wasHurt && !this.level().isClientSide()) {
            // Получаем сущность, которая нанесла урон
            if (attacker != null && !(attacker instanceof BobrittoBanditoEntity)) {
                // Находим всех BobritoBandito в радиусе 32 блоков
                this.level().getEntitiesOfClass(BobrittoBanditoEntity.class, 
                        this.getBoundingBox().inflate(32.0), 
                        bandito -> bandito != this) // исключаем самого себя
                    .forEach(bandito -> {
                        // Устанавливаем атакующего как цель для каждого бандито
                        if (attacker instanceof LivingEntity) {
                            bandito.setTarget((LivingEntity) attacker);
                            // Делаем так, чтобы бандито смотрел в сторону атакующего
                            bandito.getLookControl().setLookAt(attacker, 30.0F, 30.0F);
                        }
                    });
            }
        }
        
        return wasHurt;
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

    /**
     * Регистрирует бобритто с поселением
     */
    private void registerWithSettlement() {
        if (!this.level().isClientSide) {
            // Найти ближайшее поселение
            BlockPos townHall = SettlementManager.findNearestTownHall(this.level(), this.blockPosition());
            if (townHall != null) {
                settlementCenter = townHall;
                BobrittoManager.registerBobrito(this);
            }
        }
    }

    /**
     * Генерирует точки патрулирования вокруг поселения
     */
    public void generatePatrolPath() {
        if (settlementCenter == null) {
            // Если центр поселения не найден, используем текущую позицию
            settlementCenter = SettlementManager.findNearestTownHall(this.level(), this.blockPosition());
            if (settlementCenter == null) {
                settlementCenter = this.blockPosition();
            }
        }
        
        int radius = 30; // Радиус патрулирования вокруг поселения
        int pointCount = 8; // Количество точек для патрулирования (восьмиугольник)
        patrolPoints = new Vec3[pointCount];
        
        for (int i = 0; i < pointCount; i++) {
            double angle = 2 * Math.PI * i / pointCount;
            int x = settlementCenter.getX() + (int)(Math.cos(angle) * radius);
            int z = settlementCenter.getZ() + (int)(Math.sin(angle) * radius);
            int y = this.level().getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            patrolPoints[i] = new Vec3(x, y, z);
        }
        
        currentPatrolPoint = 0;
        completedOuterPatrol = false;
    }

    /**
     * Возвращает следующую точку патрулирования
     */
    public Vec3 getNextPatrolPoint() {
        if (patrolPoints == null || patrolPoints.length == 0) {
            return null;
        }
        
        // Если патрулируем внутри поселения
        if (getPatrolState() == BobrittoManager.PatrolState.PATROLLING_INNER) {
            // Генерируем случайную точку в пределах поселения
            int radius = 15; // Меньший радиус для патрулирования внутри
            double angle = this.random.nextDouble() * 2 * Math.PI;
            int x = settlementCenter.getX() + (int)(Math.cos(angle) * this.random.nextInt(radius));
            int z = settlementCenter.getZ() + (int)(Math.sin(angle) * this.random.nextInt(radius));
            int y = this.level().getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            return new Vec3(x, y, z);
        }
        
        // Для внешнего патрулирования используем заранее заданные точки
        Vec3 target = patrolPoints[currentPatrolPoint];
        currentPatrolPoint = (currentPatrolPoint + 1) % patrolPoints.length;
        
        // Если прошли полный круг, отмечаем это
        if (currentPatrolPoint == 0) {
            completedOuterPatrol = true;
        }
        
        return target;
    }
    
    // Геттеры и сеттеры для патрулирования
    
    public boolean isPatrolLeader() {
        return this.entityData.get(DATA_IS_PATROL_LEADER);
    }
    
    public void setPatrolLeader(boolean isLeader) {
        this.entityData.set(DATA_IS_PATROL_LEADER, isLeader);
    }
    
    public boolean isPatrolFollower() {
        return this.entityData.get(DATA_IS_PATROL_FOLLOWER);
    }
    
    public void setPatrolFollower(boolean isFollower) {
        this.entityData.set(DATA_IS_PATROL_FOLLOWER, isFollower);
    }
    
    public BobrittoManager.PatrolState getPatrolState() {
        return BobrittoManager.PatrolState.values()[this.entityData.get(DATA_PATROL_STATE)];
    }
    
    public void setPatrolState(BobrittoManager.PatrolState state) {
        this.entityData.set(DATA_PATROL_STATE, state.ordinal());
        
        // Сбрасываем точки патрулирования при изменении состояния
        if (isPatrolLeader()) {
            if (state == BobrittoManager.PatrolState.PATROLLING_OUTER) {
                generatePatrolPath();
            }
        }
    }
    
    public UUID getLeaderUUID() {
        return this.leaderUUID;
    }
    
    public void setLeaderUUID(UUID leaderUUID) {
        this.leaderUUID = leaderUUID;
    }
    
    public boolean hasCompletedOuterPatrol() {
        return completedOuterPatrol;
    }
    
    public BobrittoBanditoEntity getLeader() {
        if (this.leaderUUID == null) {
            return null;
        }
        
        for (Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate(16.0D))) {
            if (entity instanceof BobrittoBanditoEntity && entity.getUUID().equals(this.leaderUUID)) {
                return (BobrittoBanditoEntity) entity;
            }
        }
        
        return null;
    }
    
    public BlockPos getSettlementCenter() {
        return settlementCenter;
    }
    
    // NBT сохранение/загрузка
    
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("PatrolLeader", isPatrolLeader());
        tag.putBoolean("PatrolFollower", isPatrolFollower());
        tag.putInt("PatrolState", getPatrolState().ordinal());
        
        if (leaderUUID != null) {
            tag.putUUID("LeaderUUID", leaderUUID);
        }
        
        if (settlementCenter != null) {
            tag.putInt("SettlementX", settlementCenter.getX());
            tag.putInt("SettlementY", settlementCenter.getY());
            tag.putInt("SettlementZ", settlementCenter.getZ());
        }
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setPatrolLeader(tag.getBoolean("PatrolLeader"));
        setPatrolFollower(tag.getBoolean("PatrolFollower"));
        setPatrolState(BobrittoManager.PatrolState.values()[tag.getInt("PatrolState")]);
        
        if (tag.hasUUID("LeaderUUID")) {
            leaderUUID = tag.getUUID("LeaderUUID");
        }
        
        if (tag.contains("SettlementX")) {
            int x = tag.getInt("SettlementX");
            int y = tag.getInt("SettlementY");
            int z = tag.getInt("SettlementZ");
            settlementCenter = new BlockPos(x, y, z);
        }
        
        // Регистрируемся с поселением при загрузке
        registerWithSettlementTimer = 20;
    }

    @Override
    public boolean isPersistenceRequired() {
        // Предотвращаем исчезновение бобритто из поселения
        // Если бобритто привязан к поселению, он не должен исчезать
        return this.settlementCenter != null;
    }
    
    // Метод для явной установки центра поселения
    public void setSettlementCenter(BlockPos center) {
        this.settlementCenter = center;
        // Убеждаемся, что бобритто не исчезнет
        this.setPersistenceRequired();
    }
    
    /**
     * Увеличивает характеристики лидера патруля (x1.5 здоровье и урон)
     */
    public void boostLeaderStats() {
        // Увеличиваем здоровье в 1.5 раза
        float currentMaxHealth = this.getMaxHealth();
        float newMaxHealth = currentMaxHealth * 1.5f;
        
        // Устанавливаем максимальное здоровье через атрибут
        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
            .setBaseValue(newMaxHealth);
        
        // Исцеляем до полного здоровья (как бонус за назначение лидером)
        this.setHealth(this.getMaxHealth());
        
        // Увеличиваем урон в 1.5 раза
        double currentAttackDamage = this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).getBaseValue();
        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)
            .setBaseValue(currentAttackDamage * 1.5);
        
        // Увеличиваем скорость немного (на 10%)
        double currentSpeed = this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).getBaseValue();
        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
            .setBaseValue(currentSpeed * 1.1);
    }
}