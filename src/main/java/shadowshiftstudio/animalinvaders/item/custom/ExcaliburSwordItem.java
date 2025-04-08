package shadowshiftstudio.animalinvaders.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import shadowshiftstudio.animalinvaders.effect.ModEffects;
import shadowshiftstudio.animalinvaders.sound.ModSounds;

import javax.annotation.Nullable;
import java.util.List;

public class ExcaliburSwordItem extends SwordItem {
    private static final int DIVINE_PROTECTION_DURATION = 10 * 20; // 10 seconds in ticks
    private static final int COOLDOWN_DURATION = 60 * 20; // 60 seconds in ticks
    private static final float SOUND_RADIUS = 16.0F; // Sound radius in blocks
    private boolean lightningCharged = false;
    
    // Параметры для частиц
    private static final int PARTICLE_DURATION = 10 * 20; // 10 seconds in ticks
    private static final int PARTICLES_PER_TICK = 3; // Количество частиц за тик
    private static final double PARTICLE_RADIUS = 1.5D; // Радиус вокруг игрока
    private static final double PARTICLE_Y_OFFSET = 1.0D; // Смещение по Y от ног игрока
    private int particleTimer = 0;
    private Player activePlayer = null;

    public ExcaliburSwordItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Call the parent method first
        boolean result = super.hurtEnemy(stack, target, attacker);
        
        // Handle lightning strike ability if charged
        if (result && lightningCharged && attacker instanceof Player && !attacker.level().isClientSide) {
            // Create a lightning bolt at the target's position
            LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(target.level());
            if (lightningBolt != null) {
                lightningBolt.moveTo(target.position());
                lightningBolt.setCause(attacker instanceof Player ? (net.minecraft.server.level.ServerPlayer) attacker : null);
                target.level().addFreshEntity(lightningBolt);
            }
            
            // Reset the lightning charge
            lightningCharged = false;
        }
        
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        // Check cooldown
        if (!player.getCooldowns().isOnCooldown(this)) {
            // Воспроизводим звук на клиенте и сервере
            level.playSound(player, player.getX(), player.getY(), player.getZ(), 
                    ModSounds.EXCALIBUR_SPECIAL.get(), SoundSource.PLAYERS, 
                    1.0F, 1.0F);
            
            // Остальные эффекты только на сервере
            if (!level.isClientSide) {
                // Apply divine protection effect
                player.addEffect(new MobEffectInstance(ModEffects.DIVINE_PROTECTION.get(), DIVINE_PROTECTION_DURATION));
                
                // Prepare the sword for a lightning strike on next hit
                lightningCharged = true;
                
                // Запускаем эффект частиц
                particleTimer = PARTICLE_DURATION;
                activePlayer = player;
                
                // Set cooldown
                player.getCooldowns().addCooldown(this, COOLDOWN_DURATION);
            }
            
            return InteractionResultHolder.success(itemstack);
        }
        
        return InteractionResultHolder.fail(itemstack);
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        
        // Генерируем частицы только на сервере
        if (!level.isClientSide && entity instanceof Player && particleTimer > 0) {
            if (activePlayer == entity) {
                ServerLevel serverLevel = (ServerLevel) level;
                Player player = (Player) entity;
                Vec3 playerPos = player.position();
                
                // Создаем разные типы частиц
                for (int i = 0; i < PARTICLES_PER_TICK; i++) {
                    // Случайные координаты в круге вокруг игрока
                    double angle = level.random.nextDouble() * Math.PI * 2;
                    double radius = level.random.nextDouble() * PARTICLE_RADIUS;
                    double x = playerPos.x + Math.cos(angle) * radius;
                    double z = playerPos.z + Math.sin(angle) * radius;
                    double y = playerPos.y + PARTICLE_Y_OFFSET + level.random.nextDouble() * 2.0;
                    
                    // Выбираем случайный тип частиц для разнообразия
                    ParticleOptions particle;
                    int particleChoice = level.random.nextInt(5);
                    switch (particleChoice) {
                        case 0:
                            // Золотистые частицы
                            particle = ParticleTypes.GLOW;
                            break;
                        case 1:
                            // Искры от чар
                            particle = ParticleTypes.ENCHANT;
                            break;
                        case 2:
                            // Светящиеся частицы
                            particle = ParticleTypes.END_ROD;
                            break;
                        case 3:
                            // Частицы пламени (золотистые)
                            particle = ParticleTypes.FLAME;
                            break;
                        default:
                            // Частицы от удара с чарами
                            particle = ParticleTypes.ENCHANTED_HIT;
                            break;
                    }
                    
                    // Добавляем частицу в мир с нулевой скоростью, чтобы она оставалась на месте
                    serverLevel.sendParticles(particle, x, y, z, 1, 0, 0, 0, 0);
                }
                
                particleTimer--;
                
                // Если таймер закончился, сбрасываем активного игрока
                if (particleTimer <= 0) {
                    activePlayer = null;
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.animalinvaders.excalibur.tooltip").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("item.minecraft.tooltip.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }
}