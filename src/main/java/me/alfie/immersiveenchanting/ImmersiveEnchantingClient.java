package me.alfie.immersiveenchanting;

import me.alfie.immersiveenchanting.client.EnchantingTableItemRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only mod entrypoint.
 *
 * <p>Registers:</p>
 * <ul>
 *     <li>The configuration screen extension point (unchanged).</li>
 *     <li>{@link EnchantingTableItemRenderer} as the block-entity renderer for
 *         {@link BlockEntityType#ENCHANTING_TABLE}, which extends the vanilla
 *         renderer and additionally draws the floating items stored on the
 *         block entity by {@code EnchantingTableBlockEntityMixin}.</li>
 * </ul>
 *
 * <p>The renderer registration is done via {@link EventBusSubscriber} on the
 * mod event bus so it doesn't depend on the exact constructor signature
 * NeoForge expects for the client mod class.</p>
 */
@Mod(value = ImmersiveEnchanting.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ImmersiveEnchanting.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ImmersiveEnchantingClient {

    public ImmersiveEnchantingClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityType.ENCHANTING_TABLE, EnchantingTableItemRenderer::new);
    }
}
