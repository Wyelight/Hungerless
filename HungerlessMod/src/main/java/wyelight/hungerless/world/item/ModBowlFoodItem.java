package wyelight.hungerless.world.item;

import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ModBowlFoodItem extends BowlFoodItem {
    int USE_DURATION = 16;
    public ModBowlFoodItem(Properties p_40682_, int Duration) {
        super(p_40682_);
        USE_DURATION = Duration;
    }
    public int getUseDuration(@NotNull ItemStack stack) {
        return USE_DURATION;
    }
}
