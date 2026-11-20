package com.slimehoneyshields.registry;

import com.slimehoneyshields.SlimeHoneyShields;
import com.slimehoneyshields.effect.StuckEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, SlimeHoneyShields.MOD_ID);

    public static final RegistryObject<MobEffect> STUCK =
            EFFECTS.register("stuck", StuckEffect::new);

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }

    private ModEffects() {
    }
}
