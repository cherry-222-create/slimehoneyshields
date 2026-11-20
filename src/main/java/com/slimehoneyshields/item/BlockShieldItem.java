package com.slimehoneyshields.item;

import com.slimehoneyshields.client.ShieldClientExtensions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * Shared behaviour for the two block shields: both are usable shields, both can be enchanted at a
 * table, and both render through the custom shield renderer so they keep the vanilla shield model.
 */
public abstract class BlockShieldItem extends ShieldItem {
    protected BlockShieldItem(Properties properties) {
        super(properties);
    }

    /**
     * Vanilla shields cannot be enchanted; these variants are explicitly enchantable so Rebound and
     * Sticky are obtainable in normal survival play.
     */
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 15;
    }

    /**
     * Called only on the client by Forge, so the client-only renderer class is never loaded on a
     * dedicated server.
     */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ShieldClientExtensions.get());
    }
}
