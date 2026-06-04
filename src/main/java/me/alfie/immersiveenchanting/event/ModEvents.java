package me.alfie.immersiveenchanting.event;

import me.alfie.immersiveenchanting.ImmersiveEnchantingClient;
import me.alfie.immersiveenchanting.api.ApiPostEvents;
import me.alfie.immersiveenchanting.api.description.TooltipDescriptionExtensions;
import me.alfie.immersiveenchanting.command.ModCommands;
import me.alfie.immersiveenchanting.creativetab.ModCreativeTab;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostDatapack;
import me.alfie.immersiveenchanting.datapack.mod_icons.ModIconsDatapack;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundsDatapack;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.gui.ModMenus;
import me.alfie.immersiveenchanting.networking.ModPackets;
import me.alfie.immersiveenchanting.util.BookshelfChecker;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.NetworkHooks;

public class ModEvents {

    /**
     * Wires up all event listeners for the mod.
     * Registers the following on the NeoForge bus:
     * <ul>
     *   <li>Datapack reload listeners ({@link CostDatapack}, {@link NodeSoundsDatapack})</li>
     *   <li>Server lifecycle handlers (start, finished, reload, stop)</li>
     *   <li>Enchantment holder resolution on tag updates (client + server)</li>
     *   <li>Command registration ({@link ModCommands})</li>
     * </ul>
     * Registers the following on the mod event bus:
     * <ul>
     *   <li>Screen and packet registration ({@link me.alfie.immersiveenchanting.gui.ModMenus}, {@link me.alfie.immersiveenchanting.networking.ModPackets})</li>
     *   <li>Internal tooltip description extensions</li>
     *   <li>Creative tab population</li>
     * </ul>
     */
    public static void register(IEventBus modEventBus) {
        MinecraftForge.EVENT_BUS.addListener(CostDatapack::register);
        MinecraftForge.EVENT_BUS.addListener(NodeSoundsDatapack::register);
        MinecraftForge.EVENT_BUS.addListener(ModIconsDatapack::register);

        MinecraftForge.EVENT_BUS.addListener(CostDatapack::resolveClientRegistry);
        MinecraftForge.EVENT_BUS.addListener(CostDatapack::resolveServerRegistry);

        MinecraftForge.EVENT_BUS.addListener(EnchantingTableBreakHandler::onBlockBreak);
        MinecraftForge.EVENT_BUS.addListener(ModCommands::register);
        MinecraftForge.EVENT_BUS.addListener(ModEvents::onRightClickEnchantmentTable);


        modEventBus.addListener(ImmersiveEnchantingClient::registerBlockEntityRenderers);

        modEventBus.addListener(ModMenus::registerScreens);
        modEventBus.addListener(ModPackets::register);
        modEventBus.addListener(ModCreativeTab::build);

        registerPostEvents(modEventBus);
        registerInternalApiEvents(modEventBus);
    }

    private static void registerPostEvents(IEventBus modEventBus) {
        modEventBus.addListener(ApiPostEvents::postRegisterTooltipDescriptionsEvent);
    }

    private static void registerInternalApiEvents(IEventBus modEventBus) {
        modEventBus.addListener(TooltipDescriptionExtensions::registerInternalTooltipDescriptions);
    }

    private static void onRightClickEnchantmentTable(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        if(level.isClientSide) return;
        if(!(player instanceof ServerPlayer serverPlayer)) return;

        if (level.getBlockState(pos).getBlock() == Blocks.ENCHANTING_TABLE) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);

            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider(
                            (containerId, playerInventory, playerEntity) -> new EnchantingTableMenu(containerId, playerInventory, playerEntity.level(), pos),
                            Component.literal("Enchanting Table")
                    ),
                    buf -> buf.writeBlockPos(pos)
            );

            BookshelfChecker.checkBookshelves(pos, level, serverPlayer);
        }
    }
}
