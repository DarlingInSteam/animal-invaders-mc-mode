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
    
    // Характеристики для меча Festering Desire
    // Высокий урон (14), скорость атаки как у незеритового меча
    public static final Tier FESTERING = new ForgeTier(
            4,                      // уровень добычи (как у незерита)
            2031 * 2,               // долговечность (как у незерита × 2)
            9.0F,                   // скорость добычи (как у незерита)
            8.0F,                   // базовый урон (чтобы общий урон был 14 с учетом бонуса от меча)
            25,                     // повышенная зачарованность
            BlockTags.NEEDS_DIAMOND_TOOL, // тег блоков (как у незерита)
            () -> Ingredient.of(Items.DRAGON_BREATH) // материал для ремонта - дыхание дракона
    );
    
    // Характеристики для меча Excalibur
    // Легендарный меч с высоким уроном и долговечностью
    public static final Tier EXCALIBUR = new ForgeTier(
            5,                      // уровень добычи (выше незерита)
            3000,                   // долговечность (больше чем у незерита)
            10.0F,                  // скорость добычи (выше чем у незерита)
            10.0F,                  // базовый урон (чтобы общий урон был очень высоким)
            30,                     // очень высокая зачарованность
            BlockTags.NEEDS_DIAMOND_TOOL, // тег блоков (как у незерита)
            () -> Ingredient.of(Items.HEART_OF_THE_SEA) // материал для ремонта - сердце моря
    );
}