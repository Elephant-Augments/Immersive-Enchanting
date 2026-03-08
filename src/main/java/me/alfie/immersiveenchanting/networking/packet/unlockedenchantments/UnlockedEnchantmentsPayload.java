package me.alfie.immersiveenchanting.networking.packet.unlockedenchantments;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.networking.packet.PayloadHandler;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

public class UnlockedEnchantmentsPayload implements PayloadHandler<UnlockedEnchantmentsPacket> {
    @Override
    public void execOnClient(UnlockedEnchantmentsPacket packet, IPayloadContext context) {
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

    @Override //Empty
    public void execOnServer(UnlockedEnchantmentsPacket packet, IPayloadContext context) {}
}
