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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record EnchantPacket(Holder<Enchantment> enchantmentHolder, int level) implements ModNetworkPacket<EnchantPacket> {

    public static final Type<@NotNull EnchantPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "enchant_item"));

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

        ItemStack stackToEnchant = menu.getToolSlot().getItem();
        if(EnchantmentUtil.canEnchant(menu, packet.enchantmentHolder(), packet.level(), context)) {
            EnchantmentUtil.deductValidCost(menu, EnchantmentUtil.toId(packet.enchantmentHolder()), packet.level(),
                        context.player(), CostRegistry.server());

            stackToEnchant.enchant(packet.enchantmentHolder(), packet.level());
            boolean isHighestTier = packet.level() == CostRegistry.server()
                    .get(packet.enchantmentHolder())
                    .levelCosts()
                    .maxLevel();

            FxHelper.playEnchantSuccess(context.player().level(), menu.getBlockPos(), isHighestTier);
        }
    }

}
