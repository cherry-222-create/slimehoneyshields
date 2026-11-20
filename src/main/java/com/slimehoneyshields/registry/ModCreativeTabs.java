package com.slimehoneyshields.registry;

import com.slimehoneyshields.SlimeHoneyShields;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SlimeHoneyShields.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.slimehoneyshields"))
                    .icon(() -> new ItemStack(ModItems.SLIME_BLOCK_SHIELD.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.SLIME_BLOCK_SHIELD.get());
                        output.accept(ModItems.HONEY_BLOCK_SHIELD.get());
                    })
                    .build());

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }

    private ModCreativeTabs() {
    }
}
