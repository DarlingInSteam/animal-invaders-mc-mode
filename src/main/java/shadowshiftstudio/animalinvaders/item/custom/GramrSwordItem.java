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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class GramrSwordItem extends SwordItem {
    private static final int HEALTH_BOOST_DURATION = 10 * 20; // 10 seconds in ticks
    private static final int COOLDOWN_DURATION = 60 * 20; // 60 seconds in ticks
    private static final int PARTICLE_DURATION = 10 * 20; // 10 seconds in ticks
    private static final int PARTICLES_PER_TICK = 3; // Particles per tick
    private static final double PARTICLE_RADIUS = 1.5D; // Radius around player
    private static final double PARTICLE_Y_OFFSET = 1.0D; // Y offset from player's feet
    private static final float FAFNIR_BREATH_RADIUS = 5.0F; // Radius for fire breath effect
    
    private int particleTimer = 0;
    private Player activePlayer = null;

    public GramrSwordItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Call the parent method first
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        // Check cooldown
        if (!player.getCooldowns().isOnCooldown(this)) {
            // Apply effects only on server side
            if (!level.isClientSide) {
                // Apply health boost effect
                player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, HEALTH_BOOST_DURATION, 4)); // 4 = 5 hearts
                // Immediately fill the new hearts
                player.setHealth(player.getHealth() + 10.0F);
                
                // Start particle effect
                particleTimer = PARTICLE_DURATION;
                activePlayer = player;
                
                // Apply Fafnir's Breath effect - set fire to all mobs within radius
                applyFafnirBreath(level, player);
                
                // Set cooldown
                player.getCooldowns().addCooldown(this, COOLDOWN_DURATION);
            }
            
            return InteractionResultHolder.success(itemstack);
        }
        
        return InteractionResultHolder.fail(itemstack);
    }
    
    private void applyFafnirBreath(Level level, Player player) {
        // Find all living entities within the radius
        Vec3 playerPos = player.position();
        AABB areaOfEffect = new AABB(
            playerPos.x - FAFNIR_BREATH_RADIUS, 
            playerPos.y - FAFNIR_BREATH_RADIUS, 
            playerPos.z - FAFNIR_BREATH_RADIUS,
            playerPos.x + FAFNIR_BREATH_RADIUS, 
            playerPos.y + FAFNIR_BREATH_RADIUS, 
            playerPos.z + FAFNIR_BREATH_RADIUS
        );
        
        List<Entity> entities = level.getEntities(player, areaOfEffect);
        
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living && entity != player) {
                // Only affect hostile mobs
                if (entity instanceof Mob mob && mob.getMobType() != MobType.WATER) {
                    // Set entity on fire for 5 seconds
                    entity.setSecondsOnFire(5);
                }
            }
        }
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        
        // Apply fire immunity when holding or having in inventory
        if (entity instanceof Player player) {
            // Fire resistance when the sword is in inventory
            if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, false));
            }
        }
        
        // Generate particles only on server
        if (!level.isClientSide && entity instanceof Player && particleTimer > 0) {
            if (activePlayer == entity) {
                ServerLevel serverLevel = (ServerLevel) level;
                Player player = (Player) entity;
                Vec3 playerPos = player.position();
                
                // Create fire/red particles
                for (int i = 0; i < PARTICLES_PER_TICK; i++) {
                    // Random coordinates in a circle around the player
                    double angle = level.random.nextDouble() * Math.PI * 2;
                    double radius = level.random.nextDouble() * PARTICLE_RADIUS;
                    double x = playerPos.x + Math.cos(angle) * radius;
                    double z = playerPos.z + Math.sin(angle) * radius;
                    double y = playerPos.y + PARTICLE_Y_OFFSET + level.random.nextDouble() * 2.0;
                    
                    // Red/fire particles
                    ParticleOptions particle;
                    int particleChoice = level.random.nextInt(3);
                    switch (particleChoice) {
                        case 0:
                            particle = ParticleTypes.FLAME;
                            break;
                        case 1:
                            particle = ParticleTypes.LAVA;
                            break;
                        default:
                            particle = ParticleTypes.SMOKE;
                            break;
                    }
                    
                    // Add particle to the world with zero velocity
                    serverLevel.sendParticles(particle, x, y, z, 1, 0, 0, 0, 0);
                }
                
                particleTimer--;
                
                // Reset active player if timer expires
                if (particleTimer <= 0) {
                    activePlayer = null;
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.animalinvaders.gramr.tooltip").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("item.minecraft.tooltip.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }
}