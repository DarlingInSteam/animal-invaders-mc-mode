package shadowshiftstudio.animalinvaders.block;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.block.custom.BobrittoBarracksBlock;
import shadowshiftstudio.animalinvaders.block.custom.BobrittoHouseBlock;
import shadowshiftstudio.animalinvaders.block.custom.BobrittoTownHallBlock;
import shadowshiftstudio.animalinvaders.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = 
            DeferredRegister.create(ForgeRegistries.BLOCKS, AnimalInvaders.MOD_ID);

    // Town Hall - the main building that provides influence in a 200x200 block area
    public static final RegistryObject<Block> BOBRITO_TOWN_HALL = registerBlock("bobrito_town_hall",
            () -> new BobrittoTownHallBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                    .strength(5f).requiresCorrectToolForDrops()));

    // Residential Building - for bobrito population
    public static final RegistryObject<Block> BOBRITO_HOUSE = registerBlock("bobrito_house",
            () -> new BobrittoHouseBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                    .strength(3f).requiresCorrectToolForDrops()));

    // Barracks - for military bobrito
    public static final RegistryObject<Block> BOBRITO_BARRACKS = registerBlock("bobrito_barracks",
            () -> new BobrittoBarracksBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                    .strength(4f).requiresCorrectToolForDrops()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}