package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.networking.packets.EnchantmentCostRegistrySyncPacket;
import me.alfie.immersiveenchanting.networking.packets.UnlockedEnchantmentsPacket;
import net.minecraft.core.Holder;
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
     * @param packet
     * @param context
     */
    public static void onEnchantmentCostRegistrySync(final EnchantmentCostRegistrySyncPacket packet, final IPayloadContext context) {
        ImmersiveEnchanting.LOGGER.info("EnchantmentCostRegistrySync packet received on client!");

        //Build a SerializedEnchantmentCostRegistry
        SerializedEnchantmentCostRegistry serializedRegistry = new SerializedEnchantmentCostRegistry(
                packet.enchantmentNamespaces(),
                packet.levels(),
                packet.itemIds(),
                packet.amounts(),
                packet.lapisCostItemId(),
                packet.lapisCostAmount()
        );



        EnchantmentCostRegistry.setClientRegistry(
                EnchantmentCostRegistrySyncPacket.deserialize(serializedRegistry)
        );
    }

    /**
     * Send which enchantments are unlocked to the client.
     * @param packet
     * @param context
     */
    public static void onUnlockedEnchantments(final UnlockedEnchantmentsPacket packet, final IPayloadContext context) {
        Set<String> unlockedEnchantmentResourceIds = new HashSet<>(packet.enchantments());

        Set<Holder<Enchantment>> unlockedEnchantments = new HashSet<>();
        for (String resourceId : unlockedEnchantmentResourceIds) {
            ImmersiveEnchanting.getEnchantmentHolder(context.player().registryAccess(), resourceId)
                    .ifPresent(unlockedEnchantments::add);
        }

        if (context.player().containerMenu instanceof EnchantingTableMenu menu) {
            menu.setUnlockedEnchantments(unlockedEnchantments);
        }
    }
}
