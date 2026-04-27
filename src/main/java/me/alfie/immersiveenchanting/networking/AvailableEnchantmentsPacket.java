package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record AvailableEnchantmentsPacket(List<ResourceKey<Enchantment>> availableEnchantments) implements ModNetworkPacket {

    public static final PacketCodec<AvailableEnchantmentsPacket> CODEC = new PacketCodec<>() {
        @Override
        public void encode(AvailableEnchantmentsPacket packet, FriendlyByteBuf buf) {
            buf.writeInt(packet.availableEnchantments().size());

            for(ResourceKey<Enchantment> enchantmentResourceKey : packet.availableEnchantments()) {
                buf.writeResourceKey(enchantmentResourceKey);
            }
        }

        @Override
        public AvailableEnchantmentsPacket decode(FriendlyByteBuf buf) {
            int size = buf.readInt();
            List<ResourceKey<Enchantment>> result = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                result.add(buf.readResourceKey(Registries.ENCHANTMENT));
            }
            return new AvailableEnchantmentsPacket(result);
        }
    };

    @Override
    public void exec(NetworkEvent.Context context) {
        if(!context.getDirection().getReceptionSide().isClient()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if(player != null && player.containerMenu instanceof EnchantingTableMenu menu) {

            //Resolve holders
            List<Holder<Enchantment>> enchantmentHolders = new ArrayList<>(availableEnchantments().size());
            for(ResourceKey<Enchantment> enchantmentResourceKey : availableEnchantments()) {
                enchantmentHolders.add(
                        EnchantmentUtil.toHolder(enchantmentResourceKey.location(),
                                player.level().registryAccess())
                );
            }

            if(ServerConfig.areAncientBooksRequired()) {
                menu.setAvailableEnchantments(enchantmentHolders);
            } else {
                menu.setAvailableEnchantments(CostRegistry.server().getAllEnchantmentHolders());
            }


        }
    }
}
