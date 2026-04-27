package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public record RemoveEnchantmentPacket(ResourceKey<Enchantment> enchantmentResourceKey, int level) implements ModNetworkPacket {

    public static final PacketCodec<RemoveEnchantmentPacket> CODEC = new PacketCodec<RemoveEnchantmentPacket>() {
        @Override
        public void encode(RemoveEnchantmentPacket packet, FriendlyByteBuf buf) {
            buf.writeResourceKey(packet.enchantmentResourceKey());
            buf.writeInt(packet.level());
        }

        @Override
        public RemoveEnchantmentPacket decode(FriendlyByteBuf buf) {
            return new RemoveEnchantmentPacket(buf.readResourceKey(Registries.ENCHANTMENT), buf.readInt());
        }
    };

    @Override
    public void exec(NetworkEvent.Context context) {
        if(!context.getDirection().getReceptionSide().isServer()) return;
        if(!ServerConfig.isEnchantmentRemovalAllowed()) return;
        if (!(context.getSender().containerMenu instanceof EnchantingTableMenu menu)) return;

        //Resolve holder
        Holder<Enchantment> enchantmentHolder = EnchantmentUtil.toHolder(enchantmentResourceKey().location(),
                context.getSender().level().registryAccess());

        ItemStack stack = menu.getToolSlot().getItem();
        Map<Enchantment, Integer> itemEnchantments = EnchantmentHelper.getEnchantments(stack);

        int currentLevel = itemEnchantments.get(enchantmentHolder.get());
        if (currentLevel <= 0) return;

        int newLevel = currentLevel - 1;

        itemEnchantments.remove(enchantmentHolder.value());
        if (newLevel > 0) {
            itemEnchantments.put(enchantmentHolder.value(), level() - 1);
        }
        EnchantmentHelper.setEnchantments(itemEnchantments, stack);


        FxHelper.playEnchantmentRemove(context.getSender().level(), menu.getBlockPos());
    }
}
