package shadowshiftstudio.animalinvaders.item.custom;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;

public class FesteringDesireSwordItem extends SwordItem {
    private static final int INVISIBILITY_DURATION = 5 * 20; // 5 seconds in ticks
    private static final int SPEED_DURATION = 6 * 20; // 6 seconds in ticks
    private static final int SPEED_AMPLIFIER = 1; // Speed II (0 = Speed I, 1 = Speed II)
    private static final int COOLDOWN_DURATION = 60 * 20; // 60 seconds in ticks
    private static final float VAMPIRE_HEAL_PERCENT = 0.1f; // 10% healing from damage dealt

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
                
                // Set cooldown
                player.getCooldowns().addCooldown(this, COOLDOWN_DURATION);
            }
            
            return InteractionResultHolder.success(itemstack);
        }
        
        return InteractionResultHolder.fail(itemstack);
    }
}