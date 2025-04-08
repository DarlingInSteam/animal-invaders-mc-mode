package shadowshiftstudio.animalinvaders.events;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.item.LegendarySwordManager;

@Mod.EventBusSubscriber(modid = AnimalInvaders.MOD_ID)
public class LegendaryItemEvents {

    // The registry IDs for the legendary swords
    private static final String DURANDAL_ID = "animalinvaders:durandal";
    private static final String EXCALIBUR_ID = "animalinvaders:excalibur";
    private static final String FESTERING_DESIRE_ID = "animalinvaders:festering_desire";
    private static final String GRAMR_ID = "animalinvaders:gramr";

    /**
     * When an item is crafted, check if it's a legendary sword and handle the one-time crafting logic
     */
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack crafted = event.getCrafting();
        Player player = event.getEntity();

        if (player.level().isClientSide()) return;

        // Get the registry name of the crafted item
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(crafted.getItem());
        if (registryName == null) return;

        String itemId = registryName.toString();

        // Check if this is one of our legendary swords
        boolean isLegendary = itemId.equals(DURANDAL_ID) ||
                itemId.equals(EXCALIBUR_ID) ||
                itemId.equals(FESTERING_DESIRE_ID) ||
                itemId.equals(GRAMR_ID);

        if (isLegendary) {
            MinecraftServer server = player.getServer();
            if (server == null) return;

            // If it has already been crafted before, prevent crafting by replacing with a warning message
            if (LegendarySwordManager.isSwordCrafted(server, itemId)) {
                // Replace the item with air and send a message
                crafted.setCount(0);
                player.displayClientMessage(
                        Component.translatable("item.animalinvaders.legendary_sword.already_crafted")
                                .withStyle(ChatFormatting.RED),
                        true);
            } else {
                // This is the first time the sword has been crafted, mark it and notify
                LegendarySwordManager.markSwordAsCrafted(player.level(), player, crafted, itemId);
            }
        }
    }
}