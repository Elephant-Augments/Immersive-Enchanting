package me.alfie.immersiveenchanting.event;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.ApiPostEvents;
import me.alfie.immersiveenchanting.command.ModCommands;
import me.alfie.immersiveenchanting.api.datapack.manager.ClientDatapackManager;
import me.alfie.immersiveenchanting.api.datapack.manager.ServerDatapackManager;
import me.alfie.immersiveenchanting.creativetab.ModCreativeTab;
import me.alfie.immersiveenchanting.api.description.TooltipDescriptionExtensions;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostDatapack;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.mod_icons.ModIconsDatapack;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundsDatapack;
import me.alfie.immersiveenchanting.gui.ModMenus;
import me.alfie.immersiveenchanting.networking.ModPackets;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        NeoForge.EVENT_BUS.addListener(CostDatapack::register);
        NeoForge.EVENT_BUS.addListener(NodeSoundsDatapack::register);
        NeoForge.EVENT_BUS.addListener(ModIconsDatapack::register);

        NeoForge.EVENT_BUS.addListener(ServerDatapackManager::onServerStart);
        NeoForge.EVENT_BUS.addListener(ServerDatapackManager::onServerFinished);
        NeoForge.EVENT_BUS.addListener(ServerDatapackManager::onServerReload);
        NeoForge.EVENT_BUS.addListener(ServerDatapackManager::onServerStop);

        NeoForge.EVENT_BUS.addListener(ServerDatapackManager::resolveEnchantmentHolders);
        NeoForge.EVENT_BUS.addListener(ClientDatapackManager::resolveEnchantmentHolders);


        NeoForge.EVENT_BUS.addListener(ModCommands::registerCommands);
        modEventBus.addListener(ModMenus::registerScreens);

        modEventBus.addListener(ModPackets::registerClient);
        modEventBus.addListener(ModPackets::registerServer);

        modEventBus.addListener(ModCreativeTab::buildCreativeTab);

        registerPostEvents(modEventBus);
        registerInternalApiEvents();

        NeoForge.EVENT_BUS.addListener(ModEvents::sendWarningMessages);
    }

    private static void registerPostEvents(IEventBus modEventBus) {
        modEventBus.addListener(ApiPostEvents::postRegisterTooltipDescriptionsEvent);
    }

    private static void registerInternalApiEvents() {
        NeoForge.EVENT_BUS.addListener(TooltipDescriptionExtensions::registerInternalTooltipDescriptions);
    }

    public static void sendWarningMessages(PlayerEvent.PlayerLoggedInEvent event) {
        //Check if cost files are missing
        List<Holder<Enchantment>> costRegistryEnchantments = CostRegistry.client().getAllEnchantmentHolders();
        List<Holder<Enchantment>> allEnchantments = EnchantmentUtil.getAllRegisteredEnchantments(event.getEntity().registryAccess());

        Set<Holder<Enchantment>> costRegistrySet = new HashSet<>(costRegistryEnchantments);
        Set<Holder<Enchantment>> allEnchantmentsSet = new HashSet<>(allEnchantments);

        Set<Holder<Enchantment>> missingInCost = new HashSet<>(allEnchantmentsSet);
        missingInCost.removeAll(costRegistrySet);

        Set<Holder<Enchantment>> extraInCost = new HashSet<>(costRegistrySet);
        extraInCost.removeAll(allEnchantmentsSet);

        MutableComponent modIdComponent = Component.literal("[ImmersiveEnchanting] ");
        if(!missingInCost.isEmpty()) {

            Set<Identifier> missingIds = getEnchantmentIds(missingInCost);
            event.getEntity().sendSystemMessage(
                    modIdComponent.append(Component.translatable("immersiveenchanting.warn.cost_files_missing")
                            .withStyle(ChatFormatting.RED))
            );

            ImmersiveEnchanting.LOGGER.error("There are missing enchantment cost files for the following enchantments: {}", missingIds);
        }

        if(!extraInCost.isEmpty()) {

            Set<Identifier> extraIds = getEnchantmentIds(extraInCost);
            event.getEntity().sendSystemMessage(
                    modIdComponent.append(Component.translatable("immersiveenchanting.warn.too_many_cost_files")
                            .withStyle(ChatFormatting.RED))
            );

            ImmersiveEnchanting.LOGGER.error("The following enchantments are not recognised: {}", extraIds);

        }

    }

    private static Set<Identifier> getEnchantmentIds(Set<Holder<Enchantment>> enchantmentHolders) {
        return enchantmentHolders.stream()
                .map(holder -> holder.unwrapKey().orElseThrow().identifier())
                .collect(Collectors.toSet());
    }

}
