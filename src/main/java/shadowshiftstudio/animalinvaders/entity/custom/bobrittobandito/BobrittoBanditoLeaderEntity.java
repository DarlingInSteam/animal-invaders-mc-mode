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

/**
 * Отдельная сущность для лидера бобритто с особой моделью и текстурой.
 * Наследуется от обычного бобритто, но с увеличенным размером и силой.
 */
public class BobrittoBanditoLeaderEntity extends BobrittoBanditoEntity {
    
    // Множитель размера лидера (увеличен на 20%)
    private static final float LEADER_SCALE = 1.2f;
    
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
                .add(Attributes.FOLLOW_RANGE, 48.0D)      // Радиус обзора больше на 50%
                .add(Attributes.MOVEMENT_SPEED, 0.28D)    // Скорость движения больше на 12%
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
}