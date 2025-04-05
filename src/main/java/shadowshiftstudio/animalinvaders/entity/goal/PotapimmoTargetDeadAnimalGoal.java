package shadowshiftstudio.animalinvaders.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.animal.Animal;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shadowshiftstudio.animalinvaders.AnimalInvaders;
import shadowshiftstudio.animalinvaders.entity.custom.potapimmo.PotapimmoEntity;

import java.util.EnumSet;
import java.util.List;

/**
 * Цель для моба Potapimmo: следить за игроками, которые убивают животных поблизости
 * или наносят им урон, и атаковать их, если они замечены в радиусе 16 блоков.
 */
public class PotapimmoTargetDeadAnimalGoal extends TargetGoal {
    private final PotapimmoEntity potapimmo;
    private final TargetingConditions targetConditions;
    private Player targetPlayer;
    private int cooldownTicks = 0;

    public PotapimmoTargetDeadAnimalGoal(PotapimmoEntity potapimmo) {
        super(potapimmo, false, true);
        this.potapimmo = potapimmo;
        this.targetConditions = TargetingConditions.forCombat().range(16.0D).ignoreLineOfSight();
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        // Проверяем, есть ли игрок, который разозлил Potapimmo и еще виден
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }

        // Проверяем возможную цель на основе таймера злости
        if (this.potapimmo.getRemainingPersistentAngerTime() > 0) {
            if (this.potapimmo.getPersistentAngerTarget() != null) {
                Player player = this.mob.level().getPlayerByUUID(this.potapimmo.getPersistentAngerTarget());
                if (player != null && this.potapimmo.canSeePlayer(player)) {
                    this.targetPlayer = player;
                    return true;
                }
            }
            // Сбрасываем, если цель больше не видна
            this.potapimmo.setRemainingPersistentAngerTime(0);
        }

        return false;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.targetPlayer);
        this.cooldownTicks = 20; // Небольшой кулдаун между проверками
        super.start();
    }

    /**
     * Статический класс для прослушивания событий смерти и урона мобов и оповещения потапиммо
     */
    @Mod.EventBusSubscriber(modid = AnimalInvaders.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class AnimalDeathWatcher {
        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            // Проверяем, является ли умерший моб животным
            if (event.getEntity() instanceof Animal victim) {
                // Проверяем, был ли убийца игроком
                if (event.getSource().getEntity() instanceof Player player) {
                    // Находим всех потапиммо в радиусе 16 блоков
                    List<PotapimmoEntity> nearbyPotapimmos = victim.level().getEntitiesOfClass(
                            PotapimmoEntity.class,
                            victim.getBoundingBox().inflate(16.0D)
                    );

                    // Разозлить всех потапиммо, которые видят это событие
                    for (PotapimmoEntity potapimmo : nearbyPotapimmos) {
                        if (potapimmo.canSeePlayer(player)) {
                            potapimmo.angerAtPlayer(player);
                        }
                    }
                }
            }
        }
        
        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            // Проверяем, является ли пострадавший моб животным
            if (event.getEntity() instanceof Animal victim) {
                // Проверяем, был ли атакующий игроком
                if (event.getSource().getEntity() instanceof Player player) {
                    // Находим всех потапиммо в радиусе 16 блоков
                    List<PotapimmoEntity> nearbyPotapimmos = victim.level().getEntitiesOfClass(
                            PotapimmoEntity.class,
                            victim.getBoundingBox().inflate(16.0D)
                    );

                    // Разозлить всех потапиммо, которые видят это событие
                    for (PotapimmoEntity potapimmo : nearbyPotapimmos) {
                        if (potapimmo.canSeePlayer(player)) {
                            potapimmo.angerAtPlayer(player);
                        }
                    }
                }
            }
        }
    }
}