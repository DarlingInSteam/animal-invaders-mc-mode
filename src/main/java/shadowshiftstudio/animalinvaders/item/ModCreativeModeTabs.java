package shadowshiftstudio.animalinvaders.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import shadowshiftstudio.animalinvaders.AnimalInvaders;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AnimalInvaders.MOD_ID);

    // Создаем вкладку для нашего мода
    public static final RegistryObject<CreativeModeTab> ANIMAL_INVADERS_TAB = CREATIVE_MODE_TABS.register("animal_invaders_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.GOYDA_SLAYER.get()))
                    .title(Component.translatable("creativemodetab.animal_invaders_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        // Добавляем наш меч GoydaSlayer в эту вкладку
                        pOutput.accept(ModItems.GOYDA_SLAYER.get());
                        // Сюда можно добавить другие предметы в будущем
                    })
                    .build()
    );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}