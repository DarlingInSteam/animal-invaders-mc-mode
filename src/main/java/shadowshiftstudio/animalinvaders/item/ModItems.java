package shadowshiftstudio.animalinvaders.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.AxeItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.item.custom.FesteringDesireSwordItem;
import shadowshiftstudio.animalinvaders.item.custom.ExcaliburSwordItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, AnimalInvaders.MOD_ID);
    
    // Регистрируем топор GoydaSlayer
    // Урон: (6.0F базовый + 5.0F бонус топора) = 11.0F, что в 1.5 раза больше чем у незеритового (8.0F)
    // Скорость атаки: -2.7F, что лучше чем у незеритового (-3.0F)
    public static final RegistryObject<Item> GOYDA_SLAYER = ITEMS.register("goyda_slayer",
            () -> new AxeItem(ModTiers.GOYDA, 5.0F, -2.7F, 
                    new Item.Properties().fireResistant()));

    // Регистрируем меч Festering Desire
    // Базовый урон: 8.0F + бонус меча: 6.0F = 14.0F
    // Скорость атаки: -2.4F (как у незеритового меча)
    // Особые способности: вампиризм (10% от урона) и невидимость на 5 секунд при нажатии ПКМ (кулдаун 1 минута)
    public static final RegistryObject<Item> FESTERING_DESIRE = ITEMS.register("festering_desire",
            () -> new FesteringDesireSwordItem(ModTiers.FESTERING, 6, -2.4F, 
                    new Item.Properties().fireResistant()));

    // Регистрируем меч Excalibur
    // Базовый урон: 10.0F + бонус меча: 6.0F = 16.0F
    // Скорость атаки: -2.4F (как у незеритового меча)
    // Особые способности: 
    // 1) После ПКМ следующий удар вызовет молнию на цель
    // 2) Божественная защита на 10 секунд (30% снижение урона, защита от вредоносных эффектов)
    // Кулдаун 1 минута
    public static final RegistryObject<Item> EXCALIBUR = ITEMS.register("excalibur",
            () -> new ExcaliburSwordItem(ModTiers.EXCALIBUR, 6, -2.4F, 
                    new Item.Properties().fireResistant()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}