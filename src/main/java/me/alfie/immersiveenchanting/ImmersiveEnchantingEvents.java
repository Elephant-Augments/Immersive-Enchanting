package me.alfie.immersiveenchanting;

import com.mojang.brigadier.CommandDispatcher;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.alfie.immersiveenchanting.api.DescriptionLayoutExtension;
import me.alfie.immersiveenchanting.api.TooltipDescriptionExtensions;
import me.alfie.immersiveenchanting.api.internal.CostLayoutExtension;
import me.alfie.immersiveenchanting.api.internal.ReplicateLayoutExtension;
import me.alfie.immersiveenchanting.api.internal.TransmuteLayoutExtension;
import me.alfie.immersiveenchanting.creativetab.ModCreativeTab;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.EnchantmentMetadataRegistry;
import me.alfie.immersiveenchanting.gui.*;
import me.alfie.immersiveenchanting.gui.core.NodeTooltip;
import me.alfie.immersiveenchanting.gui.replicate.ReplicateNode;
import me.alfie.immersiveenchanting.gui.replicate.ReplicateNodeTooltip;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLayout;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLine;
import me.alfie.immersiveenchanting.gui.transmute.TransmuteNode;
import me.alfie.immersiveenchanting.gui.transmute.TransmuteNodeTooltip;
import me.alfie.immersiveenchanting.item.AncientBook;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.networking.ServerPayloadHandler;
import me.alfie.immersiveenchanting.networking.packets.EnchantmentCostRegistrySyncPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImmersiveEnchantingEvents {

    @SubscribeEvent
    public void onItemDamage(EntityLeaveLevelEvent event) {
        if(event.getLevel().isClientSide()) return; //Server only
        if(event.getEntity() instanceof ItemEntity itemEntity) {
            if(itemEntity.getItem().is(ModItems.ANCIENT_BOOK.get())) {
                if(itemEntity.isOnFire()) {
                    //Is it in soul fire
                    //Get block
                    BlockPos pos = itemEntity.getOnPos();
                    Block block = event.getLevel().getBlockState(pos).getBlock();
                    if(block instanceof SoulFireBlock
                            || block instanceof SoulSandBlock
                            || block.equals(Blocks.SOUL_SOIL)) {
                        ItemEntity musicDisc = new ItemEntity(
                                event.getLevel(),
                                pos.getX() + 0.5,
                                pos.getY() + 0.5,
                                pos.getZ() + 0.5,
                                new ItemStack(ModItems.BIBLIOCLASM_MUSIC_DISC.get()));

                        musicDisc.setDeltaMovement(0, 0.3, 0);
                        musicDisc.setInvulnerable(true);

                        event.getLevel().addFreshEntity(musicDisc);
                        event.getLevel().playSound(null, pos, SoundEvents.SOUL_SAND_BREAK,
                                SoundSource.BLOCKS, 0.4F, 1.2F);
                        event.getLevel().playSound(null, pos, SoundEvents.SNOW_BREAK,
                                SoundSource.BLOCKS, 0.4F, 1.2F);
                        event.getLevel().playSound(null, pos, SoundEvents.GENERIC_BURN,
                                SoundSource.BLOCKS, 0.1F, 0.5F);

                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("immersiveenchanting")
                        .then(Commands.literal("getDisabledEnchantments")
                                    .executes(context -> {
                            String disabled = String.join(", ",
                                    EnchantmentCostRegistry.getServerRegistry().getDisabledEnchantments());
                            context.getSource().sendSuccess(() -> Component.literal("Disabled enchantments: " + disabled).withStyle(ChatFormatting.RED), false);
                            return 1;
                        })
                )
        );
    }

    @SubscribeEvent
    public void onServerStart(ServerStartedEvent event) {
        ImmersiveEnchanting.ENCHANTMENT_COST_DATAPACK_HANDLER.setServer(event.getServer());
    }

    public void onLoadComplete(FMLLoadCompleteEvent event) {
        registerInternalTooltipDescriptions();
    }

    private void registerInternalTooltipDescriptions() {
        //Using the API hooks internally here to add text/custom rendering into the description box.
        TooltipDescriptionExtensions.register(new CostLayoutExtension());
        TooltipDescriptionExtensions.register(new TransmuteLayoutExtension());
        TooltipDescriptionExtensions.register(new ReplicateLayoutExtension());
    }

    public void onClientStart(FMLClientSetupEvent event) {
        EnchantmentCostRegistry.setClientRegistry(new EnchantmentCostRegistry());

        //Load client resources
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        EnchantmentMetadataRegistry.loadIcons(resourceManager);
    }

    /**
     * Request the client's enchantment cost registry to be updated on login.
     * @param event
     */
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof ServerPlayer player) {
            EnchantmentCostRegistrySyncPacket.syncClientWithServer(player);
        }
    }

    /**
     * Fires server-side, adds in datapacks.
     * @param event
     */
    @SubscribeEvent
    public void reloadListener(AddReloadListenerEvent event) {
        event.addListener(ImmersiveEnchanting.ENCHANTMENT_COST_DATAPACK_HANDLER);
    }

    /**
     * Set itemIds inside the creative tab.
     * @param event
     */
    public void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == ModCreativeTab.ENCHANTING_TAB.get()) {
            //*Add itemIds to tab.*
            event.accept(new ItemStack(Items.ENCHANTING_TABLE));
            event.accept(new ItemStack(Items.CHISELED_BOOKSHELF));
            event.accept(new ItemStack(ModItems.CREATIVE_BOOKSHELF));
            event.accept(new ItemStack(Items.LAPIS_LAZULI));
            event.accept(new ItemStack(ModItems.BIBLIOCLASM_MUSIC_DISC.get()));

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
                AncientBook.setStoredEnchantment(bookStack, holder);

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
     * @param event
     */
    @SubscribeEvent
    public void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.LIBRARIAN) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            for (int level : trades.keySet()) {
                List<VillagerTrades.ItemListing> tradeList = trades.get(level);
                if (tradeList == null) continue;

                tradeList.removeIf(listing -> listing instanceof VillagerTrades.EnchantBookForEmeralds); //Remove enchanted book trades
            }
        }
    }

    /**
     * Replace enchanting table with new GUI.
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
                serverPlayer.openMenu(
                        new SimpleMenuProvider(
                                (id, inv, p) -> new EnchantingTableMenu(id, inv, level, pos),
                                Component.literal("Enchanting Table")
                        ),
                        buf -> buf.writeBlockPos(pos)
                );
                ServerPayloadHandler.checkBookshelvesAndUpdateClient(pos, player.level(), serverPlayer);
            }
        }
    }

    /**
     * Hide the default enchantment tooltip for ancient books. Custom tooltip is rendered in AncientBook class.
     * @param event
     */
    @SubscribeEvent
    public void hideAncientBookEnchantmentTooltip(ItemTooltipEvent event) {
        if(event.getItemStack().is(ModItems.ANCIENT_BOOK.get())) {
            event.getToolTip().removeIf(component -> component.getContents() instanceof TranslatableContents contents
                    && contents.getKey().startsWith("enchantment."));
        }

    }

    /**
     * Migrate book data in containers.
     * @param event
     */
    @SubscribeEvent
    public void onContainerOpen(PlayerContainerEvent.Open event) {
        AbstractContainerMenu menu = event.getContainer();
        Level level = event.getEntity().level();
        migrateBookInContainer(menu, level);
    }

    private void migrateBookInContainer(AbstractContainerMenu menu, Level level) {
        for(Slot slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if(stack.is(ModItems.ANCIENT_BOOK.get())) {
                AncientBook.migrateDataComponent(stack, level);
            }
        }
        menu.broadcastChanges();
    }

}
