package me.alfie.immersiveenchanting.networking.packet.unlockedenchantments;

import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class UnlockedEnchantmentsPacket {
    public final List<ResourceKey<Enchantment>> enchantments;

    public UnlockedEnchantmentsPacket(List<ResourceKey<Enchantment>> enchantments) {
        this.enchantments = enchantments;
    }

    public static void encode(UnlockedEnchantmentsPacket packet, FriendlyByteBuf buf) {
        buf.writeCollection(packet.enchantments, FriendlyByteBuf::writeResourceKey);
    }

    public static UnlockedEnchantmentsPacket decode(FriendlyByteBuf buf) {
        List<ResourceKey<Enchantment>> enchantments = buf.readList(b -> b.readResourceKey(Registries.ENCHANTMENT));
        return new UnlockedEnchantmentsPacket(enchantments);
    }

    public static void handle(UnlockedEnchantmentsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(
                () -> exec(packet, contextSupplier.get()));
        contextSupplier.get().setPacketHandled(true);
    }

    public static void exec(UnlockedEnchantmentsPacket packet, NetworkEvent.Context context) {
        if(!context.getDirection().getReceptionSide().isClient()) return;
        Set<ResourceKey<Enchantment>> unlockedEnchantmentResourceIds = new HashSet<>(packet.enchantments);
        LocalPlayer player = Minecraft.getInstance().player;

        Set<Holder<Enchantment>> unlockedEnchantments = new HashSet<>();
        for (ResourceKey<Enchantment> enchantmentKey : unlockedEnchantmentResourceIds) {
            EnchantmentUtil.getEnchantmentHolder(player.level().registryAccess(), enchantmentKey)
                    .ifPresent(unlockedEnchantments::add);
        }

        if (player.containerMenu instanceof EnchantingTableMenu menu) {
            menu.setUnlockedEnchantments(unlockedEnchantments);
        }
    }
}
