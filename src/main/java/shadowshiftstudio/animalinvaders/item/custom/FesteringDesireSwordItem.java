package shadowshiftstudio.animalinvaders.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class FesteringDesireSwordItem extends SwordItem {
    private static final int INVISIBILITY_DURATION = 5 * 20; // 5 seconds in ticks
    private static final int SPEED_DURATION = 6 * 20; // 6 seconds in ticks
    private static final int SPEED_AMPLIFIER = 1; // Speed II (0 = Speed I, 1 = Speed II)
    private static final int COOLDOWN_DURATION = 60 * 20; // 60 seconds in ticks
    private static final float VAMPIRE_HEAL_PERCENT = 0.1f; // 10% healing from damage dealt
    
    private static final int PARTICLE_DURATION = 10 * 20; // 10 seconds in ticks
    private static final int PARTICLES_PER_TICK = 3; // Number of particles per tick
    private static final double PARTICLE_RADIUS = 1.5D; // Radius around the player
    private static final double PARTICLE_Y_OFFSET = 1.0D; // Y offset from the player's feet
    private int particleTimer = 0;
    private Player activePlayer = null;

    public FesteringDesireSwordItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Call the parent method first
        boolean result = super.hurtEnemy(stack, target, attacker);
        
        // Handle vampire effect - heal for 10% of damage dealt
        if (result && attacker instanceof Player player) {
            float damageDealt = getDamage();
            float healAmount = damageDealt * VAMPIRE_HEAL_PERCENT;
            
            // Heal the player
            player.heal(healAmount);
        }
        
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        // Check cooldown
        if (!player.getCooldowns().isOnCooldown(this)) {
            // Apply invisibility and speed effects
            if (!level.isClientSide) {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, INVISIBILITY_DURATION));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SPEED_DURATION, SPEED_AMPLIFIER));
                
                // Start particle effect
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
        
        // Generate particles only on the server
        if (!level.isClientSide && entity instanceof Player && particleTimer > 0) {
            if (activePlayer == entity) {
                ServerLevel serverLevel = (ServerLevel) level;
                Player player = (Player) entity;
                Vec3 playerPos = player.position();
                
                // Create dark particles
                for (int i = 0; i < PARTICLES_PER_TICK; i++) {
                    // Random coordinates in a circle around the player
                    double angle = level.random.nextDouble() * Math.PI * 2;
                    double radius = level.random.nextDouble() * PARTICLE_RADIUS;
                    double x = playerPos.x + Math.cos(angle) * radius;
                    double z = playerPos.z + Math.sin(angle) * radius;
                    double y = playerPos.y + PARTICLE_Y_OFFSET + level.random.nextDouble() * 2.0;
                    
                    // Select dark particles
                    ParticleOptions particle;
                    int particleChoice = level.random.nextInt(4);
                    switch (particleChoice) {
                        case 0:
                            particle = ParticleTypes.SMOKE;
                            break;
                        case 1:
                            particle = ParticleTypes.SOUL;
                            break;
                        case 2:
                            particle = ParticleTypes.ASH;
                            break;
                        default:
                            particle = ParticleTypes.WITCH;
                            break;
                    }
                    
                    // Add particle to the world with zero velocity to keep it stationary
                    serverLevel.sendParticles(particle, x, y, z, 1, 0, 0, 0, 0);
                }
                
                particleTimer--;
                
                // Reset active player if timer ends
                if (particleTimer <= 0) {
                    activePlayer = null;
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.animalinvaders.festering_desire.tooltip").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("item.minecraft.tooltip.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }
}