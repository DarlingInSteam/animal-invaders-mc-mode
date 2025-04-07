package shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import shadowshiftstudio.animalinvaders.block.settlement.BobrittoManager;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.AbstractVillager;
import shadowshiftstudio.animalinvaders.entity.custom.potapimmo.PotapimmoEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;

/**
 * Отдельная сущность для лидера бобритто с особой моделью и текстурой.
 * Наследуется от обычного бобритто, но с увеличенным размером и силой.
 */
public class BobrittoBanditoLeaderEntity extends BobrittoBanditoEntity {
    
    // Множитель размера лидера (увеличен на 20%)
    private static final float LEADER_SCALE = 1.8f;
    
    public BobrittoBanditoLeaderEntity(EntityType<? extends BobrittoBanditoEntity> entityType, Level level) {
        super(entityType, level);
        
        // Лидер патруля по умолчанию
        this.setPatrolLeader(true);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // Обеспечиваем, что лидер всегда остается лидером
        if (!this.isPatrolLeader()) {
            this.setPatrolLeader(true);
        }
        
        // Дополнительно лидер активно ищет потапиммо в большом радиусе
        if (!this.level().isClientSide() && this.getTarget() == null) {
            // Поиск потапиммо в радиусе 64 блоков
            this.level().getEntitiesOfClass(PotapimmoEntity.class, 
                    this.getBoundingBox().inflate(64.0), 
                    potapimmo -> potapimmo.isAlive())
                .stream()
                .findFirst()
                .ifPresent(potapimmo -> {
                    // Нашли потапиммо - устанавливаем его как цель
                    this.setTarget(potapimmo);
                    
                    // Созываем других бобритто в районе для нападения
                    this.level().getEntitiesOfClass(BobrittoBanditoEntity.class, 
                            this.getBoundingBox().inflate(32.0), 
                            bandito -> bandito != this && bandito.isAlive())
                        .forEach(bandito -> {
                            bandito.setTarget(potapimmo);
                            System.out.println("Лидер бобритто созвал сородича для атаки на потапиммо");
                        });
                    
                    System.out.println("Лидер бобритто обнаружил потапиммо и организует нападение");
                });
        }
    }
    
    /**
     * Переопределяем registerGoals для лидера, чтобы добавить
     * особые цели для атаки и больший радиус обнаружения
     */
    @Override
    protected void registerGoals() {
        // Вызываем родительский метод для базовых целей
        super.registerGoals();
        
        // Лидер имеет более высокий приоритет для обнаружения игроков и потапиммо
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PotapimmoEntity.class, true));
        
        // Лидер также охотится на жителей деревень с более высоким приоритетом
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Villager.class, true));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }
    
    /**
     * Создание атрибутов для сущности лидера.
     * Лидер сильнее обычного бобритто.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return BobrittoBanditoEntity.createAttributes()
                // Увеличиваем базовые характеристики уже на этапе создания сущности
                .add(Attributes.MAX_HEALTH, 30.0D)        // Здоровье больше на 50%
                .add(Attributes.ATTACK_DAMAGE, 6.0D)      // Урон больше на 50%
                .add(Attributes.ATTACK_SPEED, 1.2D)       // Скорость атаки больше на 20%
                .add(Attributes.FOLLOW_RANGE, 64.0D)      // Радиус обзора НАМНОГО больше для эффективной охоты
                .add(Attributes.MOVEMENT_SPEED, 0.32D)    // Скорость движения больше на 28%
                .add(Attributes.ARMOR, 8.0D)              // Броня больше на 60%
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D) // Сопротивление отбрасыванию больше на 50%
                .add(Attributes.ATTACK_KNOCKBACK, 3.0D);  // Сила отбрасывания больше на 50%
    }
    
    /**
     * Метод больше не нужен, так как атрибуты уже повышены при создании
     */
    @Override
    public void boostLeaderStats() {
        // Исцеляем до полного здоровья при необходимости
        this.setHealth(this.getMaxHealth());
    }
    
    /**
     * Трансформирует обычного Бобритто в лидера.
     * Используется при смене лидера патруля.
     */
    public static BobrittoBanditoLeaderEntity transformFromRegular(BobrittoBanditoEntity regularBobrito) {
        if (regularBobrito instanceof BobrittoBanditoLeaderEntity) {
            return (BobrittoBanditoLeaderEntity) regularBobrito;
        }
        
        Level level = regularBobrito.level();
        if (level.isClientSide()) {
            return null;
        }
        
        // Создаем нового лидера на месте обычного Бобритто
        EntityType<BobrittoBanditoLeaderEntity> leaderType = 
                (EntityType<BobrittoBanditoLeaderEntity>) shadowshiftstudio.animalinvaders.entity.ModEntities.BOBRITO_BANDITO_LEADER.get();
        
        BobrittoBanditoLeaderEntity leaderEntity = leaderType.create(level);
        if (leaderEntity != null) {
            // Копируем данные из обычного Бобритто
            leaderEntity.copyPosition(regularBobrito);
            leaderEntity.setYRot(regularBobrito.getYRot());
            leaderEntity.setXRot(regularBobrito.getXRot());
            
            // Копируем данные патрулирования
            leaderEntity.setPatrolLeader(true);
            leaderEntity.setPatrolState(regularBobrito.getPatrolState());
            leaderEntity.setSettlementCenter(regularBobrito.getSettlementCenter());
            
            // Копируем цель, если была
            LivingEntity target = regularBobrito.getTarget();
            if (target != null) {
                leaderEntity.setTarget(target);
            }
            
            // Передаем тэги для сохранения прочих данных
            CompoundTag tag = new CompoundTag();
            regularBobrito.addAdditionalSaveData(tag);
            leaderEntity.readAdditionalSaveData(tag);
            
            // Удаляем старого Бобритто и добавляем нового лидера
            regularBobrito.discard();
            level.addFreshEntity(leaderEntity);
            
            System.out.println("Transformed regular bobrito into leader at: " + 
                    leaderEntity.getX() + ", " + leaderEntity.getY() + ", " + leaderEntity.getZ());
            
            return leaderEntity;
        }
        
        return null;
    }
    
    /**
     * Переопределяем метод получения размера, чтобы лидер был крупнее
     */
    @Override
    public float getScale() {
        return super.getScale() * LEADER_SCALE;
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // Обеспечиваем, что лидер всегда остается лидером
        this.setPatrolLeader(true);
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }
    
    /**
     * Переопределяем метод смерти для выбора нового лидера группы
     */
    @Override
    public void die(DamageSource pDamageSource) {
        // Запускаем процесс выбора нового лидера до фактической смерти
        if (!this.level().isClientSide()) {
            System.out.println("Bobrito leader died: " + this.getUUID());
            // Находим группу последователей для этого лидера
            Level level = this.level();
            // Берем всех боброиттов в радиусе 32 блоков, которые следуют за этим лидером
            java.util.List<BobrittoBanditoEntity> followers = level.getEntitiesOfClass(
                BobrittoBanditoEntity.class,
                this.getBoundingBox().inflate(32.0),
                bandito -> bandito != this && bandito.isPatrolFollower() && 
                    (this.getUUID().equals(bandito.getLeaderUUID())));
                
            if (!followers.isEmpty()) {
                System.out.println("Found " + followers.size() + " followers for dead leader");
                
                // Сортируем последователей по здоровью (выбираем наиболее здорового)
                followers.sort((b1, b2) -> Float.compare(b2.getHealth(), b1.getHealth()));
                BobrittoBanditoEntity candidate = followers.get(0);
                
                // Трансформируем лучшего последователя в нового лидера
                BobrittoBanditoLeaderEntity newLeader = transformFromRegular(candidate);
                
                if (newLeader != null) {
                    System.out.println("New leader chosen: " + newLeader.getUUID());
                    
                    // Обновляем ссылку на лидера у оставшихся последователей
                    for (int i = 1; i < followers.size(); i++) {
                        BobrittoBanditoEntity follower = followers.get(i);
                        follower.setLeaderUUID(newLeader.getUUID());
                        System.out.println("Updated follower: " + follower.getUUID() + " to follow new leader");
                    }
                }
            } else {
                System.out.println("No followers found for dead leader");
            }
        }
        
        // Вызываем родительский метод для нормальной обработки смерти
        super.die(pDamageSource);
    }
}