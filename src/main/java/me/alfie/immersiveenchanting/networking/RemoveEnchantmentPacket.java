package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record RemoveEnchantmentPacket(Holder<Enchantment> enchantmentHolder, int level) implements ModNetworkPacket<RemoveEnchantmentPacket> {

    public static final Type<@NotNull RemoveEnchantmentPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "remove_enchantment"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveEnchantmentPacket> STREAM_CODEC = StreamCodec.composite(
            ModPackets.ENCHANTMENT_HOLDER_CODEC, RemoveEnchantmentPacket::enchantmentHolder,
            ByteBufCodecs.VAR_INT, RemoveEnchantmentPacket::level,
            RemoveEnchantmentPacket::new);

    @Override
    public Type<@NotNull RemoveEnchantmentPacket> typeId() {
        return TYPE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RemoveEnchantmentPacket> codec() {
        return STREAM_CODEC;
    }

    @Override
    public void exec(RemoveEnchantmentPacket packet, IPayloadContext context) {
        if (!(context.player().containerMenu instanceof EnchantingTableMenu menu)) return;

        ItemStack stack = menu.getToolSlot().getItem();
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(stack.getTagEnchantments());

        int currentLevel = mutable.getLevel(packet.enchantmentHolder());
        if (currentLevel <= 0) return;

        int newLevel = currentLevel - 1;

        if (newLevel > 0) {
            mutable.set(packet.enchantmentHolder(), newLevel);
        } else {
            mutable.removeIf(e -> e.equals(packet.enchantmentHolder()));
        }

        EnchantmentHelper.setEnchantments(stack, mutable.toImmutable());
        FxHelper.playEnchantmentRemove(context.player().level(), menu.getBlockPos());
    }

}
