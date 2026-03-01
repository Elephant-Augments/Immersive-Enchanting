package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.ImmersiveEnchantingEvents;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.cost.CostHelper;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.networking.packets.EnchantmentCostRegistrySyncPacket;
import me.alfie.immersiveenchanting.networking.packets.UnlockedEnchantmentsPacket;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Server -> Client
 */
public class ClientPayloadHandler {

    /**
     * Receive the enchantment cost registry from the server and store on the client.
     * Client only!
     * @param packet
     * @param context
     */
    public static void onEnchantmentCostRegistrySync(final EnchantmentCostRegistrySyncPacket packet, final IPayloadContext context) {
        if(!context.player().level().isClientSide) return;
        ImmersiveEnchanting.LOGGER.info("EnchantmentCostRegistrySync packet received on client!");

        //Build a SerializedEnchantmentCostRegistry
        SerializedEnchantmentCostRegistry serializedRegistry = new SerializedEnchantmentCostRegistry(
                packet.enchantmentIds(),
                packet.jsonStrings());

        EnchantmentCostRegistry.setClientRegistry(
                EnchantmentCostRegistrySyncPacket.deserialize(serializedRegistry)
        );
        ImmersiveEnchantingEvents.expandTags(EnchantmentCostRegistry.getClientRegistry());

        ImmersiveEnchanting.LOGGER.info(CostHelper.toAsciiTree(
                EnchantmentCostRegistry.getClientRegistry().getEnchantmentCost(
                        ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("minecraft:aqua_affinity"))
                ).getCostNodeForLevel(1)
        ));
    }

    /**
     * Send which enchantments are unlocked to the client.
     * @param packet
     * @param context
     */
    public static void onUnlockedEnchantments(final UnlockedEnchantmentsPacket packet, final IPayloadContext context) {
        Set<ResourceKey<Enchantment>> unlockedEnchantmentResourceIds = new HashSet<>(packet.enchantments());

        Set<Holder<Enchantment>> unlockedEnchantments = new HashSet<>();
        for (ResourceKey<Enchantment> enchantmentKey : unlockedEnchantmentResourceIds) {
            ImmersiveEnchanting.getEnchantmentHolder(context.player().registryAccess(), enchantmentKey)
                    .ifPresent(unlockedEnchantments::add);
        }

        if (context.player().containerMenu instanceof EnchantingTableMenu menu) {
            menu.setUnlockedEnchantments(unlockedEnchantments);
        }
    }
}
