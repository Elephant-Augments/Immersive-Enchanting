package me.alfie.immersiveenchanting;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.alfie.immersiveenchanting.creativetab.ModCreativeTab;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.EnchantmentMetadataRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.ModMenus;
import me.alfie.immersiveenchanting.item.AncientBookNBT;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.networking.ModPacketHandler;
import me.alfie.immersiveenchanting.networking.ServerPayloadHandler;
import me.alfie.immersiveenchanting.networking.packets.EnchantmentCostRegistrySyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImmersiveEnchantingEvents {

    @SubscribeEvent
    public void onServerStart(ServerStartedEvent event) {
        ImmersiveEnchanting.ENCHANTMENT_COST_DATAPACK_HANDLER.setServer(event.getServer());
    }

    public void onClientStart(FMLClientSetupEvent event) {
        EnchantmentCostRegistry.setClientRegistry(new EnchantmentCostRegistry());

        //Load client resources
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        EnchantmentMetadataRegistry.loadIcons(resourceManager);

        //Register EnchantingTableMenu to EnchantingTableScreen
        event.enqueueWork(
                () -> MenuScreens.register(ModMenus.ENCHANTING_TABLE_MENU.get(), EnchantingTableScreen::new)
        );
    }

    /**
     * Request the client's enchantment cost registry to be updated on login.
     *
     * @param event
     */
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EnchantmentCostRegistrySyncPacket.syncClientWithServer(player);
        }
    }

    public void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModPacketHandler::register); //Registers all packets.
    }

    /**
     * Fires server-side, adds in datapacks.
     *
     * @param event
     */
    @SubscribeEvent
    public void reloadListener(AddReloadListenerEvent event) {
        event.addListener(ImmersiveEnchanting.ENCHANTMENT_COST_DATAPACK_HANDLER);
    }

    /**
     * Set itemIds inside the creative tab.
     *
     * @param event
     */
    public void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == ModCreativeTab.ENCHANTING_TAB.get()) {
            //*Add itemIds to tab.*
            event.accept(new ItemStack(Items.ENCHANTING_TABLE));
            event.accept(new ItemStack(Items.CHISELED_BOOKSHELF));
            event.accept(new ItemStack(ModItems.CREATIVE_BOOKSHELF.get()));
            event.accept(new ItemStack(Items.LAPIS_LAZULI));

            // Track which itemIds we've already added to avoid duplicates
            Set<Item> addedItems = new HashSet<>();

            //Step 1. Add all Ancient Books first

            //Safe way to get enchantment registry while client is connected to server
            HolderLookup.RegistryLookup<Enchantment> enchantmentRegistryLookup = event.getParameters().holders().lookupOrThrow(Registries.ENCHANTMENT);
            enchantmentRegistryLookup.listElements().forEach(holder -> {
                //Get enchantment id (i.e "minecraft:respiration")
                ResourceLocation id = holder.key().location();

                //Create item stack
                ItemStack bookStack = new ItemStack(ModItems.ANCIENT_BOOK.get());

                //Set book data
                AncientBookNBT.setEnchantment(bookStack, holder);
                event.accept(bookStack);
            });

            //Step 2. Add all level cost itemIds after
            //for (var entry : ImmersiveEnchanting.cachedEnchantmentRegistry.entrySet()) {
            //    String enchantmentResourceId = entry.getKey().location().toString();
            //
            //    for (int level = 1; level <= RecipeConfig.getMaxLevel(enchantmentResourceId); level++) {
            //        ItemStack costStack = RecipeConfig.getItemCostFor(enchantmentResourceId, level);
            //       if (!costStack.isEmpty() && addedItems.add(costStack.getItem())) {
            //            output.accept(costStack.copy());
            //          }
            //     }
            //  }
        }

    }

    /**
     * Remove the enchanted books from villager trades.
     * Backport for 1.20.1
     *
     * @param event
     */
    @SubscribeEvent
    public void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.LIBRARIAN) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            for (int level : trades.keySet()) {
                List<VillagerTrades.ItemListing> tradeList = trades.get(level);
                if (tradeList == null) continue;

                for (VillagerTrades.ItemListing trade : tradeList) {
                    String className = trade.getClass().getSimpleName();
                    //So hacky but VillagerTrades.EnchantBookForEmeralds is private
                    if ("EnchantBookForEmeralds".equals(className)) {
                        tradeList.remove(trade);
                    }
                }
            }
        }
    }

    /**
     * Replace enchanting table with new GUI.
     *
     * @param event
     */
    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();

        if (!level.isClientSide && level.getBlockState(pos).getBlock() == Blocks.ENCHANTING_TABLE) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);

            // Open the menu
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer,
                        new SimpleMenuProvider(
                                (containerId, playerInventory, playerEntity) -> new EnchantingTableMenu(containerId, playerInventory, new ItemStackHandler(3), playerEntity.level(), pos),
                                Component.literal("Enchanting Table")
                        ),
                        buf -> buf.writeBlockPos(pos) // send the block position to the client
                );

                //Check nearby bookshelves, set menu's unlockedTypes.
                ServerPayloadHandler.checkBookshelvesAndUpdateClient(pos, player.level(), serverPlayer);
            }
        }
    }

}
