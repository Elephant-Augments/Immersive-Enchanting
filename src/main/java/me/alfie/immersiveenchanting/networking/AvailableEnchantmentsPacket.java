package me.alfie.immersiveenchanting.networking;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import io.netty.buffer.ByteBuf;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.EnchantmentUtil;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public record AvailableEnchantmentsPacket(List<Holder<Enchantment>> availableEnchantments) implements ModNetworkPacket<AvailableEnchantmentsPacket> {

    public static final Type<@NotNull AvailableEnchantmentsPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "available_enchantments"));


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
        Screen screen = Minecraft.getInstance().screen;

        System.out.println(context.player().containerMenu);
        if(screen instanceof EnchantingTableScreen enchantingTableScreen) {
            enchantingTableScreen.setAvailableEnchantments(packet.availableEnchantments());
        }
    }





}
