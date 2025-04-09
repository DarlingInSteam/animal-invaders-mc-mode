package shadowshiftstudio.animalinvaders.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.entity.ModEntities;
import shadowshiftstudio.animalinvaders.entity.ModSpawns;

/**
 * Класс для регистрации и применения модификаторов биомов
 */
public class ModBiomeModifiers {
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, AnimalInvaders.MOD_ID);

    public static final RegistryObject<Codec<PotapimmoSpawnBiomeModifier>> POTAPIMMO_SPAWN_MODIFIER_TYPE =
            BIOME_MODIFIER_SERIALIZERS.register("potapimmo_spawn", 
                    () -> RecordCodecBuilder.create(builder -> builder.group(
                            Codec.BOOL.optionalFieldOf("dummy", false).forGetter(m -> false)
                    ).apply(builder, b -> new PotapimmoSpawnBiomeModifier())));
    
    public static final RegistryObject<Codec<TralaleroTralalaSpawnBiomeModifier>> TRALALEROTRALALA_SPAWN_MODIFIER_TYPE =
            BIOME_MODIFIER_SERIALIZERS.register("tralalerotralala_spawn", 
                    () -> RecordCodecBuilder.create(builder -> builder.group(
                            Codec.BOOL.optionalFieldOf("dummy", false).forGetter(m -> false)
                    ).apply(builder, b -> new TralaleroTralalaSpawnBiomeModifier())));

    public static void register(IEventBus eventBus) {
        BIOME_MODIFIER_SERIALIZERS.register(eventBus);
    }

    public static class PotapimmoSpawnBiomeModifier implements BiomeModifier {
        public PotapimmoSpawnBiomeModifier() {}

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.ADD) {
                ResourceLocation biomeId = biome.unwrapKey()
                        .map(key -> key.location())
                        .orElse(null);
                
                if (biomeId != null && ModSpawns.isGreenBiome(biomeId)) {
                    builder.getMobSpawnSettings().addSpawn(
                            MobCategory.MONSTER,
                            new MobSpawnSettings.SpawnerData(
                                    ModEntities.POTAPIMMO.get(),
                                    100,
                                    3,
                                    5
                            )
                    );
                }
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return POTAPIMMO_SPAWN_MODIFIER_TYPE.get();
        }
    }
    
    public static class TralaleroTralalaSpawnBiomeModifier implements BiomeModifier {
        public TralaleroTralalaSpawnBiomeModifier() {}

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.ADD) {
                ResourceLocation biomeId = biome.unwrapKey()
                        .map(key -> key.location())
                        .orElse(null);
                
                if (biomeId != null && ModSpawns.isBeachBiome(biomeId)) {
                    builder.getMobSpawnSettings().addSpawn(
                            MobCategory.MONSTER,
                            new MobSpawnSettings.SpawnerData(
                                    ModEntities.TRALALEROTRALALA.get(),
                                    100,  // Weight
                                    2,    // Min count in the group
                                    4     // Max count in the group
                            )
                    );
                }
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return TRALALEROTRALALA_SPAWN_MODIFIER_TYPE.get();
        }
    }
}