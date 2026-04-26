package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record AvailableEnchantmentsPacket(List<Holder<Enchantment>> availableEnchantments) implements ModNetworkPacket<AvailableEnchantmentsPacket> {

    public static final Type<@NotNull AvailableEnchantmentsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "available_enchantments"));


    public static final StreamCodec<RegistryFriendlyByteBuf, AvailableEnchantmentsPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, AvailableEnchantmentsPacket packet) {
            buf.writeInt(packet.availableEnchantments.size());

            for (Holder<Enchantment> enchantmentHolder : packet.availableEnchantments) {
                ModPackets.ENCHANTMENT_HOLDER_CODEC.encode(buf, enchantmentHolder);
            }
        }

        @Override
        public @NotNull AvailableEnchantmentsPacket decode(@NotNull RegistryFriendlyByteBuf buf) {
            int size = buf.readInt();
            List<Holder<Enchantment>> result = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                result.add(ModPackets.ENCHANTMENT_HOLDER_CODEC.decode(buf));
            }

            return new AvailableEnchantmentsPacket(result);
        }

    };

    @Override
    public Type<@NotNull AvailableEnchantmentsPacket> typeId() {
        return TYPE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AvailableEnchantmentsPacket> codec() {
        return STREAM_CODEC;
    }

    @Override
    public void exec(AvailableEnchantmentsPacket packet, IPayloadContext context) {
        if(context.player().containerMenu instanceof EnchantingTableMenu menu) {

            if(ServerConfig.areAncientBooksRequired()) {
                menu.setAvailableEnchantments(packet.availableEnchantments());
            } else {
                menu.setAvailableEnchantments(CostRegistry.server().getAllEnchantmentHolders());
            }


        }
    }





}
