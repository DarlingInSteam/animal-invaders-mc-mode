package shadowshiftstudio.animalinvaders.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeTier;

public class ModTiers {
    // Характеристики для топора GoydaSlayer
    // Теперь базируемся на незерите, но с увеличенными характеристиками в 1.5 раза
    public static final Tier GOYDA = new ForgeTier(
            4,                      // уровень добычи (как у незерита)
            2031 * 2,               // долговечность (как у незерита × 2)
            10.0F,                  // скорость добычи (9.0F у незерита, увеличено)
            6.0F,                   // базовый урон (4.0F у незерита × 1.5)
            15,                     // зачарованность (незерит имеет 15)
            BlockTags.NEEDS_DIAMOND_TOOL, // тег блоков (как у незерита)
            () -> Ingredient.of(Items.NETHER_STAR) // материал для ремонта - звезда края
    );
}