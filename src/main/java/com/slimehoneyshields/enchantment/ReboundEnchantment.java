package com.slimehoneyshields.enchantment;

import com.slimehoneyshields.item.SlimeBlockShieldItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * Rebound: increases how far the slime block shield launches melee attackers and how fast it
 * reflects projectiles. Both uses are capped by the configured maximum distance/speed.
 */
public class ReboundEnchantment extends Enchantment {
    public ReboundEnchantment() {
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
        return stack.getItem() instanceof SlimeBlockShieldItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.getItem() instanceof SlimeBlockShieldItem;
    }
}
