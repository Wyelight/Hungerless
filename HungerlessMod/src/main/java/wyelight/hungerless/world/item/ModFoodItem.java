package wyelight.hungerless.world.item;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;


public class ModFoodItem extends Item {
    int USE_DURATION = 16;

    public ModFoodItem(Properties props, int Duration) {

        super(props);
        USE_DURATION = Duration;
    }

    public int getUseDuration(@NotNull ItemStack stack) {
        return USE_DURATION;
    }


}