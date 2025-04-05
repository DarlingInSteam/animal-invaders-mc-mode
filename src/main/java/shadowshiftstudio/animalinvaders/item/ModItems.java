package shadowshiftstudio.animalinvaders.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.AxeItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shadowshiftstudio.animalinvaders.AnimalInvaders;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, AnimalInvaders.MOD_ID);
    
    // Регистрируем топор GoydaSlayer
    // Урон: (6.0F базовый + 5.0F бонус топора) = 11.0F, что в 1.5 раза больше чем у незеритового (8.0F)
    // Скорость атаки: -2.7F, что лучше чем у незеритового (-3.0F)
    public static final RegistryObject<Item> GOYDA_SLAYER = ITEMS.register("goyda_slayer",
            () -> new AxeItem(ModTiers.GOYDA, 5.0F, -2.7F, 
                    new Item.Properties().fireResistant()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}