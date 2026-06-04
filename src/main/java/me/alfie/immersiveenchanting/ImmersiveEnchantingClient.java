package me.alfie.immersiveenchanting;

import me.alfie.immersiveenchanting.client.EnchantingTableItemRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;

@Mod(value = ImmersiveEnchanting.MODID)
@OnlyIn(Dist.CLIENT)
public class ImmersiveEnchantingClient {

    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityType.ENCHANTING_TABLE, EnchantingTableItemRenderer::new);
    }
}