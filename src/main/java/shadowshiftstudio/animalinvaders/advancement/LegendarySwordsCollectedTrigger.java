package shadowshiftstudio.animalinvaders.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.item.ModItems;

import java.util.HashSet;
import java.util.Set;

public class LegendarySwordsCollectedTrigger extends SimpleCriterionTrigger<LegendarySwordsCollectedTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation(AnimalInvaders.MOD_ID, "legendary_swords_collected");
    // Removed the static initialization of legendary swords set

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(player);
    }

    // Check if player has all four legendary swords
    public boolean hasAllLegendarySwords(ServerPlayer player) {
        // Create the legendary swords set dynamically
        Set<Item> legendarySwords = new HashSet<>();
        try {
            legendarySwords.add(ModItems.DURANDAL.get());
            legendarySwords.add(ModItems.EXCALIBUR.get());
            legendarySwords.add(ModItems.FESTERING_DESIRE.get());
            legendarySwords.add(ModItems.GRAMR.get());
            
            // Count how many unique legendary swords the player has
            int swordsFound = 0;
            for (Item sword : legendarySwords) {
                if (player.getInventory().contains(sword.getDefaultInstance())) {
                    swordsFound++;
                }
            }
            
            // Only return true if the player has all four legendary swords
            return swordsFound == 4;
        } catch (Exception e) {
            // Return false if registry isn't ready (this can happen during mod initialization)
            return false;
        }
    }

    // This method will be called to check and potentially trigger the advancement
    public void trigger(ServerPlayer player) {
        this.trigger(player, (instance) -> instance.matches(player));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        public TriggerInstance(ContextAwarePredicate player) {
            super(LegendarySwordsCollectedTrigger.ID, player);
        }

        public boolean matches(ServerPlayer player) {
            // Create the legendary swords set dynamically
            Set<Item> legendarySwords = new HashSet<>();
            try {
                legendarySwords.add(ModItems.DURANDAL.get());
                legendarySwords.add(ModItems.EXCALIBUR.get());
                legendarySwords.add(ModItems.FESTERING_DESIRE.get());
                legendarySwords.add(ModItems.GRAMR.get());
                
                // Count how many unique legendary swords the player has
                int swordsFound = 0;
                for (Item sword : legendarySwords) {
                    if (player.getInventory().contains(sword.getDefaultInstance())) {
                        swordsFound++;
                    }
                }
                return swordsFound == 4;
            } catch (Exception e) {
                // Return false if registry isn't ready (this can happen during mod initialization)
                return false;
            }
        }
    }
}