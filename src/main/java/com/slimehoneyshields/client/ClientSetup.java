package com.slimehoneyshields.client;

import com.slimehoneyshields.SlimeHoneyShields;
import com.slimehoneyshields.registry.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SlimeHoneyShields.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Vanilla only registers "blocking" for Items.SHIELD, which is what switches the item
            // model to the blocking display transforms.
            ItemProperties.register(ModItems.SLIME_BLOCK_SHIELD.get(),
                    new ResourceLocation("blocking"),
                    (stack, level, entity, seed) ->
                            entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.HONEY_BLOCK_SHIELD.get(),
                    new ResourceLocation("blocking"),
                    (stack, level, entity, seed) ->
                            entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        });
    }

    private ClientSetup() {
    }
}
