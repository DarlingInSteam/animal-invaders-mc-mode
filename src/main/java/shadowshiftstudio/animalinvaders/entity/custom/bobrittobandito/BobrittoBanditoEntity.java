package shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
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
import net.minecraft.world.level.ServerLevelAccessor;
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

import javax.annotation.Nullable;
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
    private MobSpawnType spawnType = MobSpawnType.NATURAL; // Тип спавна существа, по умолчанию природный

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
     * Регистрирует бобритто с поселением при спавне, учитывая правильное расположение
     */
    private void registerWithSettlement() {
        if (this.level().isClientSide) {
            return;
        }
        
        // Проверяем, был ли бобритто заспавнен игроком (через яйцо или команду summon)
        // Если да - не привязываем его к поселению
        if (spawnType == MobSpawnType.SPAWN_EGG || spawnType == MobSpawnType.COMMAND) {
            System.out.println("Bobrito was spawned by player, not registering with settlement");
            return;
        }
        
        if (settlementCenter == null) {
            // Пытаемся найти ближайший центр поселения
            BlockPos nearestTownHall = shadowshiftstudio.animalinvaders.block.settlement.SettlementManager.findNearestTownHall(this.level(), this.blockPosition());
            if (nearestTownHall != null) {
                settlementCenter = nearestTownHall;
                // При спавне без привязки к поселению, убедимся, что спавнимся правильно
                fixSpawnLocation();
            }
        }
        
        if (settlementCenter != null) {
            // Вызываем правильный метод для регистрации бобрито
            shadowshiftstudio.animalinvaders.block.settlement.BobrittoManager.registerBobrito(this);
        }
    }

    /**
     * Исправляет позицию бобритто при спавне, чтобы не оказаться внутри здания или на крыше
     */
    private void fixSpawnLocation() {
        if (this.level().isClientSide || settlementCenter == null) {
            return;
        }
        
        BlockPos currentPos = this.blockPosition();
        boolean needsRelocation = false;
        
        // Проверяем признаки спавна внутри здания или на крыше
        // 1. Проверка на нахождение в воздухе (без блока под ногами)
        if (!this.level().getBlockState(currentPos.below()).isSolid()) {
            needsRelocation = true;
            System.out.println("Bobrito is spawned in air, needs relocation");
        }
        
        // 2. Проверка на нахождение внутри блока
        if (!this.level().getBlockState(currentPos).isAir()) {
            needsRelocation = true;
            System.out.println("Bobrito is spawned inside a block, needs relocation");
        }
        
        // 3. Проверка на крышу - много блоков снизу и открытое небо сверху
        int solidBlocksDown = 0;
        for (int y = 1; y <= 4; y++) {
            if (this.level().getBlockState(currentPos.below(y)).isSolid()) {
                solidBlocksDown++;
            }
        }
        
        int airBlocksUp = 0;
        for (int y = 1; y <= 4; y++) {
            if (this.level().getBlockState(currentPos.above(y)).isAir()) {
                airBlocksUp++;
            }
        }
        
        // Если под нами много блоков и над нами много воздуха - вероятно, мы на крыше
        if (solidBlocksDown >= 3 && airBlocksUp >= 3) {
            needsRelocation = true;
            System.out.println("Bobrito is spawned on a roof, needs relocation");
        }
        
        // 4. Проверка на нахождение внутри здания - много блоков и сверху и снизу
        int solidBlocksUp = 0;
        for (int y = 1; y <= 4; y++) {
            if (this.level().getBlockState(currentPos.above(y)).isSolid()) {
                solidBlocksUp++;
            }
        }
        
        if (solidBlocksDown >= 2 && solidBlocksUp >= 1) {
            needsRelocation = true;
            System.out.println("Bobrito is spawned inside a building, needs relocation");
        }
        
        if (needsRelocation) {
            // Находим безопасное место перед домом
            BlockPos spawnLocation = findSafeSpawnLocation();
            if (spawnLocation != null) {
                this.teleportTo(spawnLocation.getX() + 0.5, spawnLocation.getY(), spawnLocation.getZ() + 0.5);
                System.out.println("Bobrito relocated to safe location: " + spawnLocation);
            }
        }
    }

    /**
     * Находит безопасное место для спавна бобритто перед домом
     */
    private BlockPos findSafeSpawnLocation() {
        if (settlementCenter == null) {
            return null;
        }
        
        // Стратегия 1: Ищем место в радиусе 20-30 блоков от центра поселения (перед домами)
        int searchRadius = 20 + this.random.nextInt(11); // 20-30 блоков
        
        // Проверяем 16 разных направлений от центра
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = 2 * Math.PI * attempt / 16;
            int xOffset = (int)(Math.cos(angle) * searchRadius);
            int zOffset = (int)(Math.sin(angle) * searchRadius);
            
            BlockPos testPos = new BlockPos(
                settlementCenter.getX() + xOffset,
                0, // Высоту определим позже
                settlementCenter.getZ() + zOffset
            );
            
            // Находим поверхность земли
            BlockPos groundPos = this.level().getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, 
                testPos
            );
            
            // Проверяем, что это действительно безопасное место
            if (isSafeSpawnLocation(groundPos)) {
                return groundPos;
            }
        }
        
        // Стратегия 2: Если не нашли безопасное место в первом проходе, 
        // ищем где-нибудь в окрестностях текущей позиции
        BlockPos currentPos = this.blockPosition();
        
        for (int attempt = 0; attempt < 20; attempt++) {
            int xOffset = this.random.nextInt(21) - 10; // -10 до +10
            int zOffset = this.random.nextInt(21) - 10; // -10 до +10
            
            BlockPos testPos = new BlockPos(
                currentPos.getX() + xOffset,
                0,
                currentPos.getZ() + zOffset
            );
            
            // Находим поверхность земли
            BlockPos groundPos = this.level().getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, 
                testPos
            );
            
            if (isSafeSpawnLocation(groundPos)) {
                return groundPos;
            }
        }
        
        // Стратегия 3: Если всё ещё не нашли, просто возьмем случайное место у центра поселения
        return this.level().getHeightmapPos(
            net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
            settlementCenter.offset(
                this.random.nextInt(11) - 5,
                0,
                this.random.nextInt(11) - 5
            )
        );
    }

    /**
     * Проверяет, является ли данная позиция безопасной для спавна бобритто
     */
    private boolean isSafeSpawnLocation(BlockPos pos) {
        // Проверка 1: Блок ниже должен быть твердым
        if (!this.level().getBlockState(pos.below()).isSolid()) {
            return false;
        }
        
        // Проверка 2: Блок на уровне ног и головы должен быть воздухом
        if (!this.level().getBlockState(pos).isAir() || 
            !this.level().getBlockState(pos.above()).isAir()) {
            return false;
        }
        
        // Проверка 3: Не должно быть слишком много твердых блоков внизу (признак крыши)
        int solidBlocksDown = 0;
        for (int y = 1; y <= 4; y++) {
            if (this.level().getBlockState(pos.below(y)).isSolid()) {
                solidBlocksDown++;
            }
        }
        
        // Проверка 4: Не должно быть твердых блоков вверху (признак внутри здания)
        int solidBlocksUp = 0;
        for (int y = 2; y <= 4; y++) {
            if (this.level().getBlockState(pos.above(y)).isSolid()) {
                solidBlocksUp++;
            }
        }
        
        // Если много блоков внизу (>3) и блоков сверху (>0), вероятно, это крыша или внутри здания
        if (solidBlocksDown >= 3 && solidBlocksUp > 0) {
            return false;
        }
        
        return true;
    }

    /**
     * Генерирует точки патрулирования вокруг поселения
     */
    public void generatePatrolPath() {
        if (settlementCenter == null) {
            return;
        }

        // Увеличиваем радиус для внешнего патрулирования (20-30 блоков от центра)
        int outerRadius = 20 + random.nextInt(11); // 20-30 блоков
        int pointCount = 8; // 8 точек для патрулирования периметра

        patrolPoints = new Vec3[pointCount];
        for (int i = 0; i < pointCount; i++) {
            double angle = 2 * Math.PI * i / pointCount;
            int x = settlementCenter.getX() + (int)(Math.cos(angle) * outerRadius);
            int z = settlementCenter.getZ() + (int)(Math.sin(angle) * outerRadius);
            
            // Находим подходящую высоту, чтобы избежать спавна на крышах
            int y = this.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            
            // Дополнительная проверка на блоки ниже, чтобы избежать спавна в воздухе
            BlockPos pos = new BlockPos(x, y, z);
            if (!this.level().getBlockState(pos.below()).isSolid()) {
                // Если блок ниже не твердый, найдем ближайший твердый блок вниз
                while (y > 0 && !this.level().getBlockState(new BlockPos(x, y - 1, z)).isSolid()) {
                    y--;
                }
            }
            
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
            
            // Находим подходящую высоту, чтобы избежать спавна на крышах/внутри домов
            int y = this.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            
            // Проверка на блоки выше и ниже
            BlockPos pos = new BlockPos(x, y, z);
            if (!this.level().getBlockState(pos.below()).isSolid() || !this.level().getBlockState(pos).isAir()) {
                // Если блок ниже не твердый или текущий блок не воздух, корректируем высоту
                if (!this.level().getBlockState(pos.below()).isSolid()) {
                    // Ищем ближайший твердый блок вниз
                    while (y > 0 && !this.level().getBlockState(new BlockPos(x, y - 1, z)).isSolid()) {
                        y--;
                    }
                } else if (!this.level().getBlockState(pos).isAir()) {
                    // Ищем воздух вверх
                    while (y < this.level().getMaxBuildHeight() && !this.level().getBlockState(new BlockPos(x, y, z)).isAir()) {
                        y++;
                    }
                }
            }
            
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

    public void incrementPatrolPoint() {
        if (patrolPoints != null && patrolPoints.length > 0) {
            currentPatrolPoint = (currentPatrolPoint + 1) % patrolPoints.length;
        }
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
        tag.putString("SpawnType", spawnType.name());
        
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
        
        if (tag.contains("SpawnType")) {
            try {
                this.spawnType = MobSpawnType.valueOf(tag.getString("SpawnType"));
            } catch (IllegalArgumentException e) {
                this.spawnType = MobSpawnType.NATURAL; // По умолчанию природный спавн
            }
        }
        
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

    /**
     * Устанавливает тип спавна бобритто (вызывается при финализации спавна)
     */
    public void setSpawnType(MobSpawnType type) {
        this.spawnType = type;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        // Сохраняем тип спавна
        this.spawnType = spawnType;
        
        return super.finalizeSpawn(level, difficulty, spawnType, spawnData, tag);
    }
}