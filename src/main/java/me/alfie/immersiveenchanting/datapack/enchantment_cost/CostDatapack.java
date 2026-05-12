package me.alfie.immersiveenchanting.datapack.enchantment_cost;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.datapack.*;
import me.alfie.immersiveenchanting.api.datapack.internal.DatapackKeys;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostData;
import me.alfie.immersiveenchanting.api.datapack.manager.ServerDatapackManager;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CostDatapack extends ModDatapack<CostData, CostRegistry> {

    private final CostRegistry DATA = new CostRegistry();

    protected CostDatapack() {
        super(CostData.CODEC, DatapackKeys.COST, CostRegistry.STREAM_CODEC);
    }

    @Override
    public CostRegistry getData() {
        return DATA;
    }

    /**
     * Called on every datapack reload. Clears and repopulates a temporary {@link CostRegistry}
     * with enabled entries. The registry is not pushed to {@link me.alfie.immersiveenchanting.datapack.manager.ServerDatapackManager}
     * yet — that happens when {@link #getBuilt()} is called by the server manager.
     */
    @Override
    protected void apply(Map<Identifier, CostData> identifierEnchantmentDataMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        getData().clear();

        int count = 0;
        for(Map.Entry<Identifier, CostData> entry : identifierEnchantmentDataMap.entrySet()) {
            Identifier id = remapIdentifierPath(entry.getKey());
            CostData data = entry.getValue();

            if(data.enabled()) {
                getData().register(id, data);
                count++;
            }
        }

        ImmersiveEnchanting.LOGGER.debug("Populated temp cost registry with {} entries, ready to pull.", count);
    }

    /**
     * Converts the datapack file path identifier (e.g. {@code minecraft/sharpness})
     * into the proper enchantment identifier form (e.g. {@code minecraft:sharpness})
     * by replacing the first path separator with a colon.
     *
     * Also preserves any additional sub-paths, allowing for hierarchical organization of enchantment cost files (e.g. {@code minecraft/weapon/sharpness} -> {@code minecraft:weapon/sharpness}).
     */
    private static Identifier remapIdentifierPath(Identifier originalId) {
        String path = originalId.getPath();

        int firstSlash = path.indexOf('/');
        if (firstSlash == -1) {
            throw new IllegalArgumentException("Invalid enchantment cost path: " + originalId);
        }

        String namespace = path.substring(0, firstSlash);
        String subPath = path.substring(firstSlash + 1);

        return Identifier.fromNamespaceAndPath(namespace, subPath);
    }

    @Override
    public void afterPull(MinecraftServer server) {
        ServerDatapackManager.get(DatapackKeys.COST).resolveEnchantmentHolders(server.registryAccess());
    }

    @Override
    public void afterSync(ServerPlayer player) {
        sendWarningMessages(player);
    }

    public static void register(AddServerReloadListenersEvent event) {
        DatapackRegistry.register(event, CostDatapack::new);
    }


    /**Send warnings if cost files are incorrectly setup*/
    private static void sendWarningMessages(ServerPlayer player) {
        List<Holder<Enchantment>> costRegistryEnchantments = CostRegistry.client().getAllEnchantmentHolders();
        List<Holder<Enchantment>> allEnchantments = EnchantmentUtil.getAllRegisteredEnchantments(player.registryAccess());

        Set<Holder<Enchantment>> costRegistrySet = new HashSet<>(costRegistryEnchantments);
        Set<Holder<Enchantment>> allEnchantmentsSet = new HashSet<>(allEnchantments);

        Set<Holder<Enchantment>> missingInCost = new HashSet<>(allEnchantmentsSet);
        missingInCost.removeAll(costRegistrySet);

        Set<Holder<Enchantment>> extraInCost = new HashSet<>(costRegistrySet);
        extraInCost.removeAll(allEnchantmentsSet);

        MutableComponent modIdComponent = Component.literal("[ImmersiveEnchanting] ");
        if(!missingInCost.isEmpty()) {

            Set<Identifier> missingIds = getEnchantmentIds(missingInCost);
            player.sendSystemMessage(
                    modIdComponent.append(Component.translatable("immersiveenchanting.warn.cost_files_missing")
                            .withStyle(ChatFormatting.RED))
            );

            ImmersiveEnchanting.LOGGER.error("There are missing enchantment cost files for the following enchantments: {}", missingIds);
        }

        if(!extraInCost.isEmpty()) {

            Set<Identifier> extraIds = getEnchantmentIds(extraInCost);
            player.sendSystemMessage(
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
