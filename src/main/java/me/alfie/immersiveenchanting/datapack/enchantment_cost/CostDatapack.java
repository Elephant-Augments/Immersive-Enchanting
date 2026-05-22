package me.alfie.immersiveenchanting.datapack.enchantment_cost;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.datapack.DatapackRegistry;
import me.alfie.immersiveenchanting.api.datapack.ModDatapack;
import me.alfie.immersiveenchanting.api.datapack.internal.DatapackKeys;
import me.alfie.immersiveenchanting.api.datapack.manager.ClientDatapackUpdatedEvent;
import me.alfie.immersiveenchanting.api.datapack.manager.ServerDatapackUpdatedEvent;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostData;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CostDatapack extends ModDatapack<CostData, CostRegistry> {

    private final CostRegistry DATA = new CostRegistry();

    public static void resolveServerRegistry(ServerDatapackUpdatedEvent event) {
        CostRegistry.server().resolveEnchantmentHolders(event.getServer().registryAccess());
        ImmersiveEnchanting.LOGGER.debug("Resolved enchantment holders for server");
        CostRegistry.server().printRegistry();
    }

    public static void resolveClientRegistry(ClientDatapackUpdatedEvent event) {
        CostRegistry.client().resolveEnchantmentHolders(event.getPlayer().registryAccess());
        ImmersiveEnchanting.LOGGER.debug("Resolved enchantment holders for client");
        CostRegistry.client().printRegistry();

        sendWarningMessages(event.getPlayer());
    }    @Override
    public CostRegistry getData() {
        return DATA;
    }

    /**
     * Send warnings if cost files are incorrectly setup
     */
    private static void sendWarningMessages(Player player) {
        List<Holder<Enchantment>> costRegistryEnchantments = CostRegistry.client().getAllEnchantmentHolders();
        List<Holder<Enchantment>> allEnchantments = EnchantmentUtil.getAllRegisteredEnchantments(player.registryAccess());

        Set<Holder<Enchantment>> costRegistrySet = new HashSet<>(costRegistryEnchantments);
        Set<Holder<Enchantment>> allEnchantmentsSet = new HashSet<>(allEnchantments);

        Set<Holder<Enchantment>> missingInCost = new HashSet<>(allEnchantmentsSet);
        missingInCost.removeAll(costRegistrySet);

        MutableComponent modIdComponent = Component.literal("[ImmersiveEnchanting] ");
        if (!missingInCost.isEmpty()) {

            Set<ResourceLocation> missingIds = getEnchantmentIds(missingInCost);
            player.sendSystemMessage(
                    modIdComponent.append(Component.translatable("immersiveenchanting.warn.cost_files_missing")
                            .withStyle(ChatFormatting.RED))
            );

            ImmersiveEnchanting.LOGGER.error("There are missing enchantment cost files for the following enchantments: {}", missingIds);
        }

        //Check enchanting fuels
        if (!CostRegistry.client().isRegistered(CostRegistry.ENCHANTING_FUELS)) {
            ImmersiveEnchanting.LOGGER.error("enchantment_costs/immersiveenchanting/enchanting_fuels.json is missing. Please add this file to your datapack.");
            player.sendSystemMessage(
                    modIdComponent.append(Component.translatable("immersiveenchanting.warn.enchanting_fuels_missing")
                            .withStyle(ChatFormatting.RED))
            );
        }

        if (!CostRegistry.client().isRegistered(CostRegistry.TRANSMUTE)) {
            player.sendSystemMessage(
                    modIdComponent.append(Component.translatable("immersiveenchanting.warn.cost_files_missing")
                            .withStyle(ChatFormatting.RED))
            );
            ImmersiveEnchanting.LOGGER.error("enchantment_costs/immersiveenchanting/transmute.json is missing. Please add this file to your datapack.");
        }

        if (!CostRegistry.client().isRegistered(CostRegistry.REPLICATE)) {
            player.sendSystemMessage(
                    modIdComponent.append(Component.translatable("immersiveenchanting.warn.cost_files_missing")
                            .withStyle(ChatFormatting.RED))
            );
            ImmersiveEnchanting.LOGGER.error("enchantment_costs/immersiveenchanting/replicate.json is missing. Please add this file to your datapack.");
        }
    }    /**
     * Called on every datapack reload. Clears and repopulates a temporary {@link CostRegistry}
     * with enabled entries. The registry is not pushed to {@link me.alfie.immersiveenchanting.datapack.manager.ServerDatapackManager}
     * yet — that happens when {@link #getBuilt()} is called by the server manager.
     */
    @Override
    protected void apply(Map<ResourceLocation, CostData> ResourceLocationEnchantmentDataMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        getData().clear();

        int count = 0;
        for (Map.Entry<ResourceLocation, CostData> entry : ResourceLocationEnchantmentDataMap.entrySet()) {
            ResourceLocation id = remapResourceLocationPath(entry.getKey());
            CostData data = entry.getValue();

            getData().register(id, data);
            count++;
        }

        ImmersiveEnchanting.LOGGER.debug("Populated temp cost registry with {} entries, ready to pull.", count);
    }

    private static Set<ResourceLocation> getEnchantmentIds(Set<Holder<Enchantment>> enchantmentHolders) {
        return enchantmentHolders.stream()
                .map(holder -> holder.unwrapKey().orElseThrow().ResourceLocation())
                .collect(Collectors.toSet());
    }    /**
     * Converts the datapack file path ResourceLocation (e.g. {@code minecraft/sharpness})
     * into the proper enchantment ResourceLocation form (e.g. {@code minecraft:sharpness})
     * by replacing the first path separator with a colon.
     * <p>
     * Also preserves any additional sub-paths, allowing for hierarchical organization of enchantment cost files (e.g. {@code minecraft/weapon/sharpness} -> {@code minecraft:weapon/sharpness}).
     */
    private static ResourceLocation remapResourceLocationPath(ResourceLocation originalId) {
        String path = originalId.getPath();

        int firstSlash = path.indexOf('/');
        if (firstSlash == -1) {
            throw new IllegalArgumentException("Invalid enchantment cost path: " + originalId);
        }

        String namespace = path.substring(0, firstSlash);
        String subPath = path.substring(firstSlash + 1);

        return ResourceLocation.fromNamespaceAndPath(namespace, subPath);
    }

    public static void register(AddServerReloadListenersEvent event) {
        DatapackRegistry.register(event, CostDatapack::new);
    }

    protected CostDatapack() {
        super(CostData.CODEC, DatapackKeys.COST, CostRegistry.STREAM_CODEC);
    }







}
