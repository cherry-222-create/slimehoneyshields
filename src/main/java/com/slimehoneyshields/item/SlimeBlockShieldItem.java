package com.slimehoneyshields.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SlimeBlockShieldItem extends BlockShieldItem {
    public SlimeBlockShieldItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairMaterial) {
        return repairMaterial.is(Items.SLIME_BALL)
                || repairMaterial.is(Items.SLIME_BLOCK)
                || super.isValidRepairItem(stack, repairMaterial);
    }
}
