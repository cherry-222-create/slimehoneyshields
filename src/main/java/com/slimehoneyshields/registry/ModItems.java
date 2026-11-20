package com.slimehoneyshields.registry;

import com.slimehoneyshields.SlimeHoneyShields;
import com.slimehoneyshields.item.HoneyBlockShieldItem;
import com.slimehoneyshields.item.SlimeBlockShieldItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    /** Vanilla shields have 336 durability, and 336 * 1.5 = 504. */
    public static final int SHIELD_DURABILITY = 504;

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SlimeHoneyShields.MOD_ID);

    public static final RegistryObject<Item> SLIME_BLOCK_SHIELD = ITEMS.register("slime_block_shield",
            () -> new SlimeBlockShieldItem(new Item.Properties().durability(SHIELD_DURABILITY)));

    public static final RegistryObject<Item> HONEY_BLOCK_SHIELD = ITEMS.register("honey_block_shield",
            () -> new HoneyBlockShieldItem(new Item.Properties().durability(SHIELD_DURABILITY)));

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    private ModItems() {
    }
}
