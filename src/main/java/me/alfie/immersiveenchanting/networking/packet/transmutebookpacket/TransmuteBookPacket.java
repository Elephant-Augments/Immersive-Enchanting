package me.alfie.immersiveenchanting.networking.packet.transmutebookpacket;

import me.alfie.immersiveenchanting.datacomponent.ReplicatedNBT;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.cost.CostDefinition;
import me.alfie.immersiveenchanting.datapack.cost.CostEntry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.item.AncientBook;
import me.alfie.immersiveenchanting.util.CostHelper;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

public class TransmuteBookPacket {
    public TransmuteBookPacket() {
    }

    public static void encode(TransmuteBookPacket packet, FriendlyByteBuf buf) {}

    public static TransmuteBookPacket decode(FriendlyByteBuf buf) {
        return new TransmuteBookPacket();
    }

    public static void handle(TransmuteBookPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(
                () -> exec(packet, contextSupplier.get()));
        contextSupplier.get().setPacketHandled(true);
    }

    public static void exec(TransmuteBookPacket packet, NetworkEvent.Context context) {
        Player player = context.getSender();
        Level level = player.level();
        AbstractContainerMenu menu = player.containerMenu;

        if(menu instanceof EnchantingTableMenu enchantingTableMenu) {
            ItemStack ancientBookStack = enchantingTableMenu.getToolSlotItem();
            Set<Holder<Enchantment>> unlockedEnchantments = enchantingTableMenu.getUnlockedEnchantments();

            //Get all enchantments
            List<Holder.Reference<Enchantment>> enchantments = new ArrayList<>(EnchantmentUtil.getAllEnchantments(player.level()));

            //Remove all enchantments that are already in bookshelf
            enchantments.removeIf(unlockedEnchantments::contains);

            //If all enchantments are unlocked already, pick any random enchantment.
            if(enchantments.isEmpty()) enchantments = new ArrayList<>(EnchantmentUtil.getAllEnchantments(player.level()));

            Random random = new Random();
            int randomIndex = random.nextInt(enchantments.size());
            Holder<Enchantment> randomEnchantment = enchantments.get(randomIndex);

            //Check cost
            boolean isBookReplicated = ReplicatedNBT.isReplicated(ancientBookStack);
            int playerXp = player.experienceLevel;
            ItemStack costSlotItemStack = enchantingTableMenu.getCostSlotItem();
            List<ItemStack> insertedItems = new ArrayList<>();
            insertedItems.add(costSlotItemStack);

            CostDefinition costNode = EnchantmentCostRegistry.getServerRegistry().getTransmuteCost().getCostForLevel(1);
            CostEntry validCost = CostHelper.findValidCost(costNode, insertedItems, playerXp);

            boolean hasEnoughCost = player.isCreative()
                    || CostHelper.isCostValid(validCost);

            if (hasEnoughCost && !isBookReplicated) {
                if(player.isCreative()) validCost = CostEntry.EMPTY;
                assert validCost != null;

                CostHelper.deductCost(validCost, costSlotItemStack, player);

                AncientBook.setStoredEnchantment(ancientBookStack, randomEnchantment);
                BlockPos tablePos = enchantingTableMenu.getBlockPos();
                FxHelper.playTransmuteFx(level, tablePos);

                ItemStack stack = enchantingTableMenu.getToolSlotItem().copyAndClear();
                ItemEntity entity = new ItemEntity(
                        level,
                        tablePos.getX() + 0.5,
                        tablePos.getY() + 1,
                        tablePos.getZ() + 0.5,
                        stack);
                entity.setPickUpDelay(40);
                entity.setDeltaMovement(Vec3.ZERO);
                level.addFreshEntity(entity);

                Enchantment newEnchantment = randomEnchantment.value();
                Component enchantmentName = Component.translatable(newEnchantment.getDescriptionId()).withStyle(ChatFormatting.GOLD);
                Component text = Component.translatable("gui.immersiveenchanting.transmuted_to",
                                enchantmentName)
                        .withStyle(ChatFormatting.GRAY);


                player.displayClientMessage(text, true);
                player.closeContainer();
            } else {
                FxHelper.playEnchantFailFx(level, enchantingTableMenu.getBlockPos());
            }
        }
    }
}
