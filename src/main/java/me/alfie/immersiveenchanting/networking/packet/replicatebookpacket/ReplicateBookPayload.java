package me.alfie.immersiveenchanting.networking.packet.replicatebookpacket;

import me.alfie.immersiveenchanting.datacomponent.ModDataComponents;
import me.alfie.immersiveenchanting.datacomponent.ReplicatedDataComponent;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.cost.CostEntry;
import me.alfie.immersiveenchanting.datapack.cost.CostHelper;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.networking.packet.PayloadHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
            ItemStack costSlotItem = enchantingTableMenu.getCostSlotItem();
            List<ItemStack> costItems = new ArrayList<>();
            costItems.add(costSlotItem);

            CostEntry validCost = CostHelper.findValidCost(
                    EnchantmentCostRegistry.getServerRegistry().getReplicateCost().getCostForLevel(1),
                    costItems,
                    player.experienceLevel);

            if(validCost != null || player.isCreative()) {
                player.giveExperienceLevels(-validCost.xpLevels());
                enchantingTableMenu.getCostSlotItem().shrink(validCost.amount());

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

                //Play effects
                level.playSound(null, tablePos, SoundEvents.ALLAY_ITEM_GIVEN,
                        SoundSource.BLOCKS, 0.7F, 1.2F);
                level.playSound(null, tablePos, SoundEvents.VILLAGER_WORK_CARTOGRAPHER,
                        SoundSource.BLOCKS, 0.8F, 1.5F);
                level.playSound(null, tablePos, SoundEvents.BOOK_PAGE_TURN,
                        SoundSource.BLOCKS, 0.5F, 1.5F);
                level.playSound(null, tablePos, SoundEvents.ILLUSIONER_MIRROR_MOVE,
                        SoundSource.BLOCKS, 0.4F, 1.2F);

                int particleCount = 30;
                ((ServerLevel) level).sendParticles(
                        ParticleTypes.END_ROD,
                        tablePos.getX() + 0.5,
                        tablePos.getY() + 1,
                        tablePos.getZ() + 0.5,
                        particleCount,
                        0.1, 0.1, 0.1,
                        0.1);

                player.closeContainer();
            } else {
                //If unable to enchant
                level.playSound(null, player.blockPosition(), SoundEvents.VAULT_CLOSE_SHUTTER,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
    }
}
