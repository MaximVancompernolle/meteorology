package net.pacmanmvc.meteorology.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public abstract class AbstractRodItem extends Item {
    public AbstractRodItem(Settings settings) {
        super(settings);
    }

    public abstract TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand);

    public int getEnchantability() {
        return 1;
    }
}
