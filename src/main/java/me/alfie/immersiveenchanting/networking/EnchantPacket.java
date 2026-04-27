package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
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

public record EnchantPacket(ResourceKey<Enchantment> enchantmentResourceKey,
                            int level) implements ModNetworkPacket {

    public static final PacketCodec<EnchantPacket> CODEC = new PacketCodec<>(){
        @Override
        public void encode(EnchantPacket packet, FriendlyByteBuf buf) {
            buf.writeResourceKey(packet.enchantmentResourceKey());
            buf.writeInt(packet.level());

        }

        @Override
        public EnchantPacket decode(FriendlyByteBuf buf) {
            return new EnchantPacket(buf.readResourceKey(Registries.ENCHANTMENT), buf.readInt());
        }
    };

    @Override
    public void exec(NetworkEvent.Context context) {
        if(!context.getDirection().getReceptionSide().isServer()) return;
        if(!(context.getSender().containerMenu instanceof EnchantingTableMenu menu)) return;
        //Resolve holder
        Holder<Enchantment> enchantmentHolder = EnchantmentUtil.toHolder(enchantmentResourceKey().location(),
                context.getSender().level().registryAccess());

        ItemStack stackToEnchant = menu.getToolSlot().getItem();
        if(EnchantmentUtil.canEnchant(menu, enchantmentHolder, level(), context)) {
            EnchantmentUtil.deductValidCost(menu, EnchantmentUtil.toId(enchantmentHolder), level(),
                    context.getSender(), CostRegistry.server());

            //PORT INFO 1.20.1 .enchant() doesn't overwrite enchantments, they're appended.
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stackToEnchant);
            enchantments.remove(enchantmentHolder.value());
            enchantments.put(enchantmentHolder.get(), level());
            EnchantmentHelper.setEnchantments(enchantments, stackToEnchant);

            boolean isHighestTier = level() == CostRegistry.server()
                    .get(enchantmentHolder)
                    .levelCosts()
                    .maxLevel();

            FxHelper.playEnchantSuccess(context.getSender().level(), menu.getBlockPos(), isHighestTier);
        }
    }
}
