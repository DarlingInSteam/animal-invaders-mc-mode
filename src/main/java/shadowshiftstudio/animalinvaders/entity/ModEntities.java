package shadowshiftstudio.animalinvaders.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.entity.custom.bobrittobandito.BobrittoBanditoEntity;
import shadowshiftstudio.animalinvaders.entity.custom.bullet.BulletEntity;
import shadowshiftstudio.animalinvaders.entity.custom.potapimmo.PotapimmoEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AnimalInvaders.MOD_ID);

    public static final RegistryObject<EntityType<PotapimmoEntity>> POTAPIMMO =
            ENTITIES.register("potapimmo", () -> EntityType.Builder.of(PotapimmoEntity::new, MobCategory.MONSTER)
                    .sized(1f, 2.7f).build("potapimmo"));

    public static final RegistryObject<EntityType<BobrittoBanditoEntity>> BOBRITO_BANDITO =
            ENTITIES.register("bobrittobandito", () -> EntityType.Builder.of(BobrittoBanditoEntity::new, MobCategory.MONSTER)
                    .sized(1f, 1.8f).build("bobrittobandito"));
                    
    public static final RegistryObject<EntityType<BulletEntity>> BULLET =
            ENTITIES.register("bullet", () -> EntityType.Builder.<BulletEntity>of(BulletEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("bullet"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
