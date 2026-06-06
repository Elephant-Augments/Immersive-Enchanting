package me.alfie.immersiveenchanting;

import me.alfie.immersiveenchanting.client.EnchantingTableItemRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

@OnlyIn(Dist.CLIENT)
public class ImmersiveEnchantingClient {

    protected static void init(IEventBus modEventBus) {
        modEventBus.addListener(ImmersiveEnchantingClient::registerBlockEntityRenderers);
    }

    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityType.ENCHANTING_TABLE, EnchantingTableItemRenderer::new);
    }
}