package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record TransmutePacket() implements ModNetworkPacket {

    public static final PacketCodec<TransmutePacket> CODEC = new PacketCodec<TransmutePacket>() {
        @Override
        public void encode(TransmutePacket packet, FriendlyByteBuf buf) {

        }

        @Override
        public TransmutePacket decode(FriendlyByteBuf buf) {
            return new TransmutePacket();
        }
    };

    @Override
    public void exec(NetworkEvent.Context context) {
        if(!context.getDirection().getReceptionSide().isServer()) return;
        if(!ServerConfig.isAllowTransmute()) return;

        Player player = context.getSender();
        Level level = player.level();
        if(!(player.containerMenu instanceof EnchantingTableMenu menu)) return;

        ItemStack ancientBookStack = menu.getToolSlot().getItem();
        List<Holder<Enchantment>> availableEnchantments = menu.getAvailableEnchantments();
        List<Holder<Enchantment>> allEnchantments = CostRegistry.server().getAllEnchantmentHolders();

        allEnchantments.removeIf(holder ->
                availableEnchantments.stream().anyMatch(av -> av.value().equals(holder.value())));
        if(allEnchantments.isEmpty()) allEnchantments = CostRegistry.server().getAllEnchantmentHolders();

        RandomSource random = level.getRandom();
        int randomIndex = random.nextInt(allEnchantments.size());
        Holder<Enchantment> newEnchantment = allEnchantments.get(randomIndex);
        Holder<Enchantment> oldEnchantment = EnchantmentUtil.getStoredEnchantment(ancientBookStack, player.level().registryAccess());

        if(EnchantmentUtil.canTransmute(menu, oldEnchantment, context)) {
            EnchantmentUtil.deductValidCost(menu, CostRegistry.TRANSMUTE, 1, player, CostRegistry.server());

            EnchantmentUtil.setStoredEnchantment(ancientBookStack, newEnchantment);
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

            Component newEnchantmentName = Component.translatable(newEnchantment.get().getDescriptionId()).withStyle(ChatFormatting.GOLD);
            Component actionBarMessage = Component.translatable("immersiveenchanting.action_bar.transmute_success",
                    newEnchantmentName).withStyle(ChatFormatting.GRAY);

            player.displayClientMessage(actionBarMessage, true);
            player.closeContainer();

            FxHelper.playTransmute((ServerLevel) level, tablePos);
        }
    }
}
