package shadowshiftstudio.animalinvaders.entity.custom.bullet;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class BulletEntity extends AbstractArrow {
    private int life;

    public BulletEntity(EntityType<? extends BulletEntity> entityType, Level level) {
        super(entityType, level);
        this.life = 0;
    }
    
    public BulletEntity(EntityType<? extends BulletEntity> entityType, double x, double y, double z, Level level) {
        super(entityType, x, y, z, level);
        this.life = 0;
    }
    
    public BulletEntity(EntityType<? extends BulletEntity> entityType, LivingEntity shooter, Level level) {
        super(entityType, shooter, level);
        this.life = 0;
    }
    
    @Override
    public void tick() {
        super.tick();
        // Ограничиваем время жизни пули
        if (++this.life > 40) {
            this.discard();
        }
    }
    
    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.STONE_HIT;
    }
    
    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        // Дополнительная логика при попадании в сущность, если требуется
    }
    
    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        // Вызывает дополнительные эффекты при столкновении с блоками
        if (!this.level().isClientSide) {
            this.discard(); // Удаляем пулю при столкновении
        }
    }
    
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // Пулю нельзя подобрать
    }
    
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}