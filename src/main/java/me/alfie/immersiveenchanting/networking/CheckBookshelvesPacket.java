package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.EnchantmentUtil;
import me.alfie.immersiveenchanting.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record CheckBookshelvesPacket(BlockPos enchantingTablePos) implements ModNetworkPacket<CheckBookshelvesPacket> {
    public static final Type<@NotNull CheckBookshelvesPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "check_bookshelves"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CheckBookshelvesPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            CheckBookshelvesPacket::enchantingTablePos,
            CheckBookshelvesPacket::new);

    @Override
    public Type<@NotNull CheckBookshelvesPacket> typeId() {
        return TYPE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CheckBookshelvesPacket> codec() {
        return STREAM_CODEC;
    }

    @Override
    public void exec(CheckBookshelvesPacket packet, IPayloadContext context) {
        List<ChiseledBookShelfBlockEntity> bookshelves = getNearbyBookshelves(packet.enchantingTablePos(), context.player().level());
        List<Holder<Enchantment>> result = new ArrayList<>();

        for(ChiseledBookShelfBlockEntity bookshelf : bookshelves) {
            List<ItemStack> books = getBooks(bookshelf);

            for(ItemStack stack : books) {
                if(stack.getItem() == ModItems.ANCIENT_BOOK.get()) {
                    Holder<Enchantment> enchantmentHolder = EnchantmentUtil.getStoredEnchantment(stack);
                    if(enchantmentHolder != null) result.add(enchantmentHolder);
                }
            }
        }

        PacketDistributor.sendToPlayer((ServerPlayer) context.player(), new AvailableEnchantmentsPacket(result));
    }


    /**
     * Searches for nearby chiseled bookshelves within a predefined radius.
     *
     * <p>The scan forms a hollow rectangular prism around the enchanting table,
     * similar to vanilla enchanting mechanics.</p>
     *
     * @return A list of nearby {@link ChiseledBookShelfBlockEntity} instances
     */
    private List<ChiseledBookShelfBlockEntity> getNearbyBookshelves(BlockPos blockPos, Level level) {
        List<ChiseledBookShelfBlockEntity> result = new ArrayList<>();

        int radiusX = 2;
        int radiusY = 3;
        int radiusZ = 2;

        for (int dy = 0; dy < radiusY; dy++) {
            for (int rX = 2; rX <= radiusX; rX++) {
                for (int rZ = 2; rZ <= radiusZ; rZ++) {
                    for (int dx = -rX; dx <= rX; dx++) {
                        for (int dz = -rZ; dz <= rZ; dz++) {
                            if (Math.abs(dx) != rX && Math.abs(dz) != rZ) continue;

                            BlockPos checkPos = blockPos.offset(dx, dy, dz);
                            BlockEntity blockEntity = level.getBlockEntity(checkPos);

                            if (blockEntity instanceof ChiseledBookShelfBlockEntity chiseledBookshelf) result.add(chiseledBookshelf);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Retrieves all item stacks stored within a chiseled bookshelf.
     *
     * @param bookshelf The bookshelf block entity
     * @return A list of all 6 item slots contained in the bookshelf
     */
    private List<ItemStack> getBooks(ChiseledBookShelfBlockEntity bookshelf) {
        List<ItemStack> result = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            result.add(bookshelf.getItem(i));
        }

        return result;
    }


}
