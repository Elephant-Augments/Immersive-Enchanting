package me.alfie.immersiveenchanting.networking.packet.replicatebookpacket;

import me.alfie.immersiveenchanting.datacomponent.ReplicatedNBT;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.cost.CostDefinition;
import me.alfie.immersiveenchanting.datapack.cost.CostEntry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.CostHelper;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ReplicateBookPacket {
    public ReplicateBookPacket() {
    }

    public static void encode(ReplicateBookPacket packet, FriendlyByteBuf buf) {}

    public static ReplicateBookPacket decode(FriendlyByteBuf buf) {
        return new ReplicateBookPacket();
    }

    public static void handle(ReplicateBookPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(
                () -> exec(packet, contextSupplier.get()));
        contextSupplier.get().setPacketHandled(true);
    }

    public static void exec(ReplicateBookPacket packet, NetworkEvent.Context context) {
        Level level = context.getSender().level();
        Player player = context.getSender();


        if(player.containerMenu instanceof EnchantingTableMenu enchantingTableMenu) {
            int playerXp = player.experienceLevel;
            ItemStack costSlotItemStack = enchantingTableMenu.getCostSlotItem();
            List<ItemStack> insertedItems = new ArrayList<>();
            insertedItems.add(costSlotItemStack);

            CostDefinition costNode = EnchantmentCostRegistry.getServerRegistry().getReplicateCost().getCostForLevel(1);
            CostEntry validCost = CostHelper.findValidCost(costNode, insertedItems, playerXp);

            boolean hasEnoughCost = player.isCreative()
                    || CostHelper.isCostValid(validCost);

            if (hasEnoughCost) {
                if(player.isCreative()) validCost = CostEntry.EMPTY;

                assert validCost != null;
                CostHelper.deductCost(validCost, costSlotItemStack, player);

                //Create new stack with replicated tag
                ItemStack oldStack = enchantingTableMenu.getToolSlotItem().copyAndClear();
                ItemStack newStack = oldStack.copy();

                ReplicatedNBT.setReplicated(newStack, true);

                BlockPos tablePos = enchantingTableMenu.getBlockPos();

                //Create 2 entities
                for (int i = 0; i < 2; i++) {
                    ItemStack stackToSpawn = (i == 0) ? oldStack : newStack;
                    ItemEntity entity = new ItemEntity(
                            level,
                            tablePos.getX() + 0.5,
                            tablePos.getY() + 1,
                            tablePos.getZ() + 0.5,
                            stackToSpawn);
                    entity.setPickUpDelay(40);
                    entity.setDeltaMovement(Vec3.ZERO);
                    level.addFreshEntity(entity);
                }

                FxHelper.playReplicateFx(level, tablePos);

                player.closeContainer();
            } else {
                FxHelper.playEnchantFailFx(level, enchantingTableMenu.getBlockPos());
            }
        }
    }
}
