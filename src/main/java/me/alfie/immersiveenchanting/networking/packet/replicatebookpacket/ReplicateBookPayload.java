package me.alfie.immersiveenchanting.networking.packet.replicatebookpacket;

import me.alfie.immersiveenchanting.datacomponent.ModDataComponents;
import me.alfie.immersiveenchanting.datacomponent.ReplicatedDataComponent;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.cost.CostDefinition;
import me.alfie.immersiveenchanting.datapack.cost.CostEntry;
import me.alfie.immersiveenchanting.util.CostHelper;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.networking.packet.PayloadHandler;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class ReplicateBookPayload implements PayloadHandler<ReplicateBookPacket> {
    @Override //Empty
    public void execOnClient(ReplicateBookPacket packet, IPayloadContext context) {}

    @Override
    public void execOnServer(ReplicateBookPacket packet, IPayloadContext context) {
        Level level = context.player().level();
        Player player = context.player();


        if(context.player().containerMenu instanceof EnchantingTableMenu enchantingTableMenu) {
            int playerXp = player.experienceLevel;
            ItemStack costSlotItemStack = enchantingTableMenu.getCostSlotItem();
            List<ItemStack> insertedItems = new ArrayList<>();
            insertedItems.add(costSlotItemStack);

            CostDefinition costNode = EnchantmentCostRegistry.getServerRegistry().getReplicateCost().getCostForLevel(1);
            CostEntry validCost = CostHelper.findValidCost(costNode, insertedItems, playerXp);

            boolean hasEnoughCost = player.hasInfiniteMaterials()
                    || CostHelper.isCostValid(validCost);

            if (hasEnoughCost) {
                if(player.hasInfiniteMaterials()) validCost = CostEntry.EMPTY;

                assert validCost != null;
                CostHelper.deductCost(validCost, costSlotItemStack, player);

                //Create new stack with replicated tag
                ItemStack oldStack = enchantingTableMenu.getToolSlotItem().copyAndClear();
                ItemStack newStack = oldStack.copy();
                newStack.set(ModDataComponents.REPLICATED, new ReplicatedDataComponent(true));

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
