package me.alfie.immersiveenchanting.networking.packet.removeenchantment;

import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.cost.CostDefinition;
import me.alfie.immersiveenchanting.datapack.cost.CostEntry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.networking.packet.enchantitem.EnchantItemPacket;
import me.alfie.immersiveenchanting.util.CostHelper;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class RemoveEnchantmentPacket {

    public final ResourceKey<Enchantment> enchantment;
    public final int enchantmentLevel;

    public RemoveEnchantmentPacket(ResourceKey<Enchantment> enchantment, int enchantmentLevel) {
        this.enchantment = enchantment;
        this.enchantmentLevel = enchantmentLevel;
    }

    public static void encode(RemoveEnchantmentPacket packet, FriendlyByteBuf buf) {
        buf.writeResourceKey(packet.enchantment);
        buf.writeInt(packet.enchantmentLevel);
    }

    public static RemoveEnchantmentPacket decode(FriendlyByteBuf buf) {
        ResourceKey<Enchantment> enchantment = buf.readResourceKey(Registries.ENCHANTMENT);
        int enchantmentLevel = buf.readInt();
        return new RemoveEnchantmentPacket(enchantment, enchantmentLevel);
    }

    public static void handle(RemoveEnchantmentPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(
                () -> exec(packet, contextSupplier.get()));
        contextSupplier.get().setPacketHandled(true);
    }

    public static void exec(RemoveEnchantmentPacket packet, NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player == null) return;
        Level level = player.level();

        AbstractContainerMenu enchantingTableMenu = player.containerMenu;
        ItemStack itemToEnchant = enchantingTableMenu.getSlot(EnchantingTableMenu.SLOTS.TOOL.ordinal()).getItem();

        Optional<Holder.Reference<Enchantment>> enchantmentHolder = EnchantmentUtil.getEnchantmentHolder(
                level.registryAccess(),
                packet.enchantment);

        Holder<Enchantment> enchantment = enchantmentHolder.orElseThrow(() ->
                new IllegalStateException("Enchantment not found: " + packet.enchantment)
        );

        Map<Enchantment, Integer> itemEnchantments = EnchantmentHelper.getEnchantments(itemToEnchant);
        itemEnchantments.remove(enchantment.value()); //Remove old one

        if(packet.enchantmentLevel > 1) {
            itemEnchantments.put(enchantment.value(), packet.enchantmentLevel-1); //Add new one
        }
        EnchantmentHelper.setEnchantments(itemEnchantments, itemToEnchant);

        FxHelper.playEnchantmentRemoveSound(level, player);
    }
}
