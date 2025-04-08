package shadowshiftstudio.animalinvaders.advancement;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.item.ModItems;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = AnimalInvaders.MOD_ID)
public class AdvancementEvents {
    // No static initialization of sword set here - will be created dynamically when needed

    // Check for legendary swords when inventory changes
    @SubscribeEvent
    public static void onInventoryChanged(PlayerEvent.ItemPickupEvent event) {
        checkForLegendarySwords(event.getEntity());
    }

    // Also check when player logs in
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        checkForLegendarySwords(event.getEntity());
    }

    // And when player respawns
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        checkForLegendarySwords(event.getEntity());
    }

    // Main method to check for all swords and trigger advancement
    private static void checkForLegendarySwords(Player player) {
        if (player.level().isClientSide() || !(player instanceof ServerPlayer)) {
            return;
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;
        
        // Create the legendary swords set dynamically when needed
        // This avoids static initialization issues at mod load time
        Set<Item> legendarySwords = new HashSet<>();
        try {
            legendarySwords.add(ModItems.DURANDAL.get());
            legendarySwords.add(ModItems.EXCALIBUR.get());
            legendarySwords.add(ModItems.FESTERING_DESIRE.get());
            legendarySwords.add(ModItems.GRAMR.get());
        
            // Count how many unique legendary swords the player has
            int swordsFound = 0;
            for (Item sword : legendarySwords) {
                if (serverPlayer.getInventory().contains(sword.getDefaultInstance())) {
                    swordsFound++;
                }
            }
            
            // Only trigger if the player has all four legendary swords
            if (swordsFound == 4) {
                ModTriggers.getLegendarySwordsCollectedTrigger().trigger(serverPlayer);
            }
        } catch (Exception e) {
            // Catch exceptions to prevent crashes when registry isn't ready
            // This can happen during mod initialization
        }
    }
}