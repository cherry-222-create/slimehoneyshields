package com.slimehoneyshields;

import com.mojang.logging.LogUtils;
import com.slimehoneyshields.registry.ModCreativeTabs;
import com.slimehoneyshields.registry.ModEffects;
import com.slimehoneyshields.registry.ModEnchantments;
import com.slimehoneyshields.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Slime &amp; Honey Shields.
 *
 * <p>Two shield variants that turn a successful block into a counter-attack:
 * the slime shield launches the attacker away, the honey shield glues them in place for a few
 * seconds. Both also react to incoming projectiles.</p>
 */
@Mod(SlimeHoneyShields.MOD_ID)
public final class SlimeHoneyShields {
    public static final String MOD_ID = "slimehoneyshields";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SlimeHoneyShields() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modBus);
        ModEnchantments.register(modBus);
        ModEffects.register(modBus);
        ModCreativeTabs.register(modBus);

        modBus.addListener(SlimeHoneyShields::addItemsToVanillaTabs);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private static void addItemsToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.SLIME_BLOCK_SHIELD);
            event.accept(ModItems.HONEY_BLOCK_SHIELD);
        }
    }
}
