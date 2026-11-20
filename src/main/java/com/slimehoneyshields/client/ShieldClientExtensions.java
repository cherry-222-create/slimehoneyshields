package com.slimehoneyshields.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

@OnlyIn(Dist.CLIENT)
public final class ShieldClientExtensions {
    private static final IClientItemExtensions INSTANCE = new IClientItemExtensions() {
        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return ShieldBEWLR.get();
        }
    };

    public static IClientItemExtensions get() {
        return INSTANCE;
    }

    private ShieldClientExtensions() {
    }
}
