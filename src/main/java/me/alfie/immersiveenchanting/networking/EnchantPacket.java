package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.BookshelfChecker;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record EnchantPacket(Holder<Enchantment> enchantmentHolder, int level) implements ModNetworkPacket<EnchantPacket> {

    public static final Type<@NotNull EnchantPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "enchant_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantPacket> STREAM_CODEC = StreamCodec.composite(
            ModPackets.ENCHANTMENT_HOLDER_CODEC, EnchantPacket::enchantmentHolder,
            ByteBufCodecs.VAR_INT, EnchantPacket::level,
            EnchantPacket::new);

    @Override
    public Type<@NotNull EnchantPacket> typeId() {
        return TYPE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, EnchantPacket> codec() {
        return STREAM_CODEC;
    }

    @Override
    public void exec(EnchantPacket packet, IPayloadContext context) {
        if(!(context.player().containerMenu instanceof EnchantingTableMenu menu)) return;

        List<Holder<Enchantment>> availableEnchantments = BookshelfChecker.getEnchantmentsInBookshelves(menu.getBlockPos(), context.player().level());
        if(!availableEnchantments.contains(packet.enchantmentHolder())) return;

        ItemStack stackToEnchant = menu.getToolSlot().getItem();
        if(!stackToEnchant.supportsEnchantment(packet.enchantmentHolder())) return;
        int equippedLevel = stackToEnchant.getEnchantmentLevel(packet.enchantmentHolder());
        if (packet.level() != equippedLevel + 1) return;



        stackToEnchant.enchant(packet.enchantmentHolder(), packet.level());
    }
}
