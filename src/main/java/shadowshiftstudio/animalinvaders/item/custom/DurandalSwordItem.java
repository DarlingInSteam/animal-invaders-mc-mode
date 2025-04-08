package shadowshiftstudio.animalinvaders.item.custom;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import shadowshiftstudio.animalinvaders.effect.ModEffects;

public class DurandalSwordItem extends SwordItem {
    private static final int LAST_STAND_DURATION = 5 * 20; // 5 seconds in ticks
    private static final int COOLDOWN_DURATION = 60 * 20; // 60 seconds in ticks
    private static final int PARTICLE_DURATION = 10 * 20; // 10 seconds in ticks
    private static final int PARTICLES_PER_TICK = 3; // Particles per tick
    private static final double PARTICLE_RADIUS = 1.5D; // Radius around player
    private static final double PARTICLE_Y_OFFSET = 1.0D; // Y offset from player's feet
    
    private boolean explosiveAttackReady = false;
    private int particleTimer = 0;
    private Player activePlayer = null;

    public DurandalSwordItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Call the parent method first
        boolean result = super.hurtEnemy(stack, target, attacker);
        
        // Handle explosive attack if ready
        if (result && explosiveAttackReady && attacker instanceof Player && !attacker.level().isClientSide) {
            // Create explosion at target position
            Level level = target.level();
            // Fixed method call to match the correct signature in Minecraft 1.20.1
            level.explode(null, target.getX(), target.getY(), target.getZ(), 
                    4.0F, // Power like TNT
                    Level.ExplosionInteraction.NONE); // No block damage
            
            // Reset the explosive attack ready state
            explosiveAttackReady = false;
        }
        
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        // Check cooldown
        if (!player.getCooldowns().isOnCooldown(this)) {
            // Apply effects only on server side
            if (!level.isClientSide) {
                // Apply Last Stand effect
                player.addEffect(new MobEffectInstance(ModEffects.LAST_STAND.get(), LAST_STAND_DURATION));
                
                // Prepare the sword for an explosive attack
                explosiveAttackReady = true;
                
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
        
        // Generate particles only on server
        if (!level.isClientSide && entity instanceof Player && particleTimer > 0) {
            if (activePlayer == entity) {
                ServerLevel serverLevel = (ServerLevel) level;
                Player player = (Player) entity;
                Vec3 playerPos = player.position();
                
                // Create gold particles
                for (int i = 0; i < PARTICLES_PER_TICK; i++) {
                    // Random coordinates in a circle around the player
                    double angle = level.random.nextDouble() * Math.PI * 2;
                    double radius = level.random.nextDouble() * PARTICLE_RADIUS;
                    double x = playerPos.x + Math.cos(angle) * radius;
                    double z = playerPos.z + Math.sin(angle) * radius;
                    double y = playerPos.y + PARTICLE_Y_OFFSET + level.random.nextDouble() * 2.0;
                    
                    // Golden particles
                    ParticleOptions particle;
                    int particleChoice = level.random.nextInt(3);
                    switch (particleChoice) {
                        case 0:
                            particle = ParticleTypes.GLOW;
                            break;
                        case 1:
                            particle = ParticleTypes.FLAME;
                            break;
                        default:
                            particle = ParticleTypes.SCRAPE;
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
}