package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record TransmutePacket() implements ModNetworkPacket<TransmutePacket> {

    public static final Type<@NotNull TransmutePacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "transmute"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TransmutePacket> STREAM_CODEC = StreamCodec.unit(new TransmutePacket());

    @Override
    public Type<@NotNull TransmutePacket> typeId() {
        return TYPE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, TransmutePacket> codec() {
        return STREAM_CODEC;
    }

    @Override
    public void exec(TransmutePacket packet, IPayloadContext context) {
        Player player = context.player();
        Level level = player.level();
        if(!(player.containerMenu instanceof EnchantingTableMenu menu)) return;

        ItemStack ancientBookStack = menu.getToolSlot().getItem();
        List<Holder<Enchantment>> availableEnchantments = menu.getAvailableEnchantments();
        List<Holder<Enchantment>> allEnchantments = CostRegistry.server().getAllEnchantmentHolders();

        allEnchantments.removeIf(availableEnchantments::contains);
        if(allEnchantments.isEmpty()) allEnchantments = CostRegistry.server().getAllEnchantmentHolders();

        RandomSource random = level.getRandom();
        int randomIndex = random.nextInt(allEnchantments.size());
        Holder<Enchantment> newEnchantment = allEnchantments.get(randomIndex);
        if(EnchantmentUtil.canTransmute(menu, newEnchantment, context)) {
            EnchantmentUtil.deductValidCost(menu, CostRegistry.TRANSMUTE, 1, player, CostRegistry.server());

            EnchantmentUtil.setStoredEnchantment(ancientBookStack, newEnchantment, level);
            BlockPos tablePos = menu.getBlockPos();

            ItemStack newBookStack = menu.getToolSlot().getItem().copyAndClear();
            ItemEntity itemEntity = new ItemEntity(
                    level,
                    tablePos.getX() + 0.5,
                    tablePos.getY() + 1,
                    tablePos.getZ() + 0.5,
                    newBookStack);
            itemEntity.setPickUpDelay(40);
            itemEntity.setDeltaMovement(Vec3.ZERO);
            level.addFreshEntity(itemEntity);

            Component newEnchantmentName = newEnchantment.value().description().copy().withStyle(ChatFormatting.GOLD);
            Component actionBarMessage = Component.translatable("immersiveenchanting.action_bar.transmute_success",
                    newEnchantmentName).withStyle(ChatFormatting.GRAY);

            player.sendOverlayMessage(actionBarMessage);
            player.closeContainer();

            FxHelper.playTransmute((ServerLevel) level, tablePos);
        }
    }
}
