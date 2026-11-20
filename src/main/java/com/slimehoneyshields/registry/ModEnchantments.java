package com.slimehoneyshields.registry;

import com.slimehoneyshields.SlimeHoneyShields;
import com.slimehoneyshields.enchantment.ReboundEnchantment;
import com.slimehoneyshields.enchantment.StickyEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, SlimeHoneyShields.MOD_ID);

    public static final RegistryObject<Enchantment> REBOUND =
            ENCHANTMENTS.register("rebound", ReboundEnchantment::new);

    public static final RegistryObject<Enchantment> STICKY =
            ENCHANTMENTS.register("sticky", StickyEnchantment::new);

    public static void register(IEventBus modBus) {
        ENCHANTMENTS.register(modBus);
    }

    private ModEnchantments() {
    }
}
