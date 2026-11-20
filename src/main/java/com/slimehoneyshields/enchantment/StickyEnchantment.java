package com.slimehoneyshields.enchantment;

import com.slimehoneyshields.item.HoneyBlockShieldItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * Sticky: extends the honey block shield's immobilise duration by one second per level.
 */
public class StickyEnchantment extends Enchantment {
    public StickyEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.BREAKABLE,
                new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 5 + (level - 1) * 9;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 20;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof HoneyBlockShieldItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.getItem() instanceof HoneyBlockShieldItem;
    }
}
