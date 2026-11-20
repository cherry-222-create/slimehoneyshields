package com.slimehoneyshields.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HoneyBlockShieldItem extends BlockShieldItem {
    public HoneyBlockShieldItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairMaterial) {
        return repairMaterial.is(Items.HONEYCOMB)
                || repairMaterial.is(Items.HONEY_BOTTLE)
                || repairMaterial.is(Items.HONEY_BLOCK)
                || super.isValidRepairItem(stack, repairMaterial);
    }
}
