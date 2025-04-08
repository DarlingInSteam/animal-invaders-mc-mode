package shadowshiftstudio.animalinvaders.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashSet;
import java.util.Set;

/**
 * Manages the crafting of legendary swords to ensure each can only be crafted once.
 */
public class LegendarySwordManager {
    private static final String SAVE_DATA_NAME = "animalinvaders_legendary_swords";
    private static final String SWORD_LIST_KEY = "SwordList";

    // Create a SavedData subclass to store our data
    public static class LegendarySwordsData extends SavedData {
        private final Set<String> craftedSwords = new HashSet<>();

        public LegendarySwordsData() {
            // Default constructor
        }

        public static LegendarySwordsData create() {
            return new LegendarySwordsData();
        }

        public static LegendarySwordsData load(CompoundTag tag) {
            LegendarySwordsData data = create();
            ListTag swordList = tag.getList(SWORD_LIST_KEY, 8); // 8 is the tag type for String

            for (int i = 0; i < swordList.size(); i++) {
                data.craftedSwords.add(swordList.getString(i));
            }

            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            ListTag swordList = new ListTag();

            for (String swordId : craftedSwords) {
                swordList.add(StringTag.valueOf(swordId));
            }

            tag.put(SWORD_LIST_KEY, swordList);
            return tag;
        }

        public boolean isSwordCrafted(String swordId) {
            return craftedSwords.contains(swordId);
        }

        public boolean markSwordAsCrafted(String swordId) {
            if (craftedSwords.contains(swordId)) {
                return false;
            }

            craftedSwords.add(swordId);
            setDirty(); // Mark as dirty so it gets saved
            return true;
        }
    }

    // Get the data from the server
    private static LegendarySwordsData getData(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(LegendarySwordsData::load, LegendarySwordsData::create, SAVE_DATA_NAME);
    }

    /**
     * Checks if a legendary sword has been crafted before.
     *
     * @param server The Minecraft server
     * @param swordId The identifier of the sword (item registry name)
     * @return true if the sword has been crafted before, false otherwise
     */
    public static boolean isSwordCrafted(MinecraftServer server, String swordId) {
        return getData(server).isSwordCrafted(swordId);
    }

    /**
     * Marks a sword as crafted and broadcasts a message to all players.
     *
     * @param level The level/world
     * @param player The player who crafted the sword
     * @param stack The sword ItemStack
     * @param swordId The identifier of the sword (item registry name)
     * @return true if the sword was successfully marked as crafted, false if it was already crafted
     */
    public static boolean markSwordAsCrafted(Level level, Player player, ItemStack stack, String swordId) {
        if (level.isClientSide()) {
            return false;
        }

        MinecraftServer server = level.getServer();
        if (server == null) {
            return false;
        }

        // Try to mark the sword as crafted in our saved data
        if (!getData(server).markSwordAsCrafted(swordId)) {
            return false; // Sword was already crafted
        }

        // Broadcast message to all players
        Component message = Component.translatable("item.animalinvaders.legendary_sword.crafted",
                        player.getDisplayName(),
                        Component.literal(stack.getHoverName().getString()).withStyle(ChatFormatting.GOLD))
                .withStyle(ChatFormatting.LIGHT_PURPLE);

        server.getPlayerList().broadcastSystemMessage(message, false);

        return true;
    }
}