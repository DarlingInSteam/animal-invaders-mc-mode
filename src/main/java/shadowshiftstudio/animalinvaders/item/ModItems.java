package shadowshiftstudio.animalinvaders.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.AxeItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.item.custom.DurandalSwordItem;
import shadowshiftstudio.animalinvaders.item.custom.FesteringDesireSwordItem;
import shadowshiftstudio.animalinvaders.item.custom.ExcaliburSwordItem;
import shadowshiftstudio.animalinvaders.item.custom.GramrSwordItem;

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
    // Базовый урон: 5.0F + бонус меча: 6.0F = 11.0F
    // Скорость атаки: -2.4F (как у незеритового меча)
    // Особые способности: вампиризм (10% от урона) и невидимость на 5 секунд при нажатии ПКМ (кулдаун 1 минута)
    // + темные частицы вокруг игрока на 10 секунд
    public static final RegistryObject<Item> FESTERING_DESIRE = ITEMS.register("festering_desire",
            () -> new FesteringDesireSwordItem(ModTiers.FESTERING, 6, -2.4F, 
                    new Item.Properties().fireResistant()));

    // Регистрируем меч Excalibur
    // Базовый урон: 5.0F + бонус меча: 6.0F = 11.0F
    // Скорость атаки: -2.4F (как у незеритового меча)
    // Особые способности: 
    // 1) После ПКМ следующий удар вызовет молнию на цель
    // 2) Божественная защита на 10 секунд (30% снижение урона, защита от вредоносных эффектов)
    // Кулдаун 1 минута
    public static final RegistryObject<Item> EXCALIBUR = ITEMS.register("excalibur",
            () -> new ExcaliburSwordItem(ModTiers.EXCALIBUR, 6, -2.4F, 
                    new Item.Properties().fireResistant()));
                    
    // Регистрируем меч Durandal
    // Базовый урон: 5.0F + бонус меча: 6.0F = 11.0F
    // Скорость атаки: -2.4F (как у незеритового меча)
    // Особые способности: 
    // 1) Эффект "Последний рубеж" на 5 секунд (уровень здоровья не может упасть ниже 5 сердец
    //    и атаки наносят на 30% больше урона)
    // 2) Взрывная атака (урон как от TNT, без урона блокам и самому игроку)
    // 3) Золотые частицы вокруг игрока на 10 секунд
    // Кулдаун 1 минута
    public static final RegistryObject<Item> DURANDAL = ITEMS.register("durandal",
            () -> new DurandalSwordItem(ModTiers.DURANDAL, 6, -2.4F, 
                    new Item.Properties().fireResistant()));

    // Регистрируем меч Gramr
    // Базовый урон: 5.0F + бонус меча: 6.0F = 11.0F
    // Скорость атаки: -2.4F (как у незеритового меча)
    // Особые способности: 
    // 1) Иммунитет к лаве и огню (пассивный эффект)
    // 2) "Дыхание Фафнира" - поджигает всех враждебных мобов в радиусе 5 блоков
    // 3) Дает 5 дополнительных сердец на 10 секунд
    // 4) Красные/огненные частицы вокруг игрока на 10 секунд
    // Кулдаун 1 минута
    public static final RegistryObject<Item> GRAMR = ITEMS.register("gramr",
            () -> new GramrSwordItem(ModTiers.EXCALIBUR, 6, -2.4F, 
                    new Item.Properties().fireResistant()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}