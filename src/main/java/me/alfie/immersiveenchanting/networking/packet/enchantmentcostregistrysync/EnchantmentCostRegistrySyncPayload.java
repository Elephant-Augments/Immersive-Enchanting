package me.alfie.immersiveenchanting.networking.packet.enchantmentcostregistrysync;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.ImmersiveEnchantingEvents;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.cost.CostHelper;
import me.alfie.immersiveenchanting.networking.packet.PayloadHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EnchantmentCostRegistrySyncPayload implements PayloadHandler<EnchantmentCostRegistrySyncPacket> {
    @Override
    public void execOnClient(EnchantmentCostRegistrySyncPacket packet, IPayloadContext context) {
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
                ).getCostForLevel(1)
        ));
    }

    @Override //Empty
    public void execOnServer(EnchantmentCostRegistrySyncPacket packet, IPayloadContext context) {}
}
