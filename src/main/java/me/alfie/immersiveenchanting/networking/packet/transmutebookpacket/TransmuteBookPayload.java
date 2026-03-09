package me.alfie.immersiveenchanting.networking.packet.transmutebookpacket;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datacomponent.ReplicatedDataComponent;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.cost.CostEntry;
import me.alfie.immersiveenchanting.datapack.cost.CostHelper;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.item.AncientBook;
import me.alfie.immersiveenchanting.lootmodifier.AncientBookLootModifier;
import me.alfie.immersiveenchanting.networking.packet.PayloadHandler;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TransmuteBookPayload implements PayloadHandler<TransmuteBookPacket> {
    @Override
    public void execOnClient(TransmuteBookPacket packet, IPayloadContext context) {}

    @Override
    public void execOnServer(TransmuteBookPacket packet, IPayloadContext context) {
        Player player = context.player();
        Level level = player.level();
        AbstractContainerMenu menu = player.containerMenu;


        if(menu instanceof EnchantingTableMenu enchantingTableMenu) {
            ItemStack ancientBookStack = enchantingTableMenu.getToolSlotItem();
            Set<Holder<Enchantment>> unlockedEnchantments = enchantingTableMenu.getUnlockedEnchantments();

            //Get all enchantments
            List<Holder.Reference<Enchantment>> enchantments = new ArrayList<>(
                    EnchantmentUtil.getAllEnchantments(player.level()));

            //Remove all enchantments that are already in bookshelf
            enchantments.removeIf(unlockedEnchantments::contains);

            //If all enchantments are unlocked already, pick any random enchantment.
            if(enchantments.isEmpty()) {
                enchantments = new ArrayList<>(
                        EnchantmentUtil.getAllEnchantments(player.level()));
            }

            Random random = new Random();
            int randomIndex = random.nextInt(enchantments.size());
            Holder<Enchantment> randomEnchantment = enchantments.get(randomIndex);

            //Check cost
            ItemStack costSlotItem = enchantingTableMenu.getCostSlotItem();
            List<ItemStack> costItems = new ArrayList<>();
            costItems.add(costSlotItem);

            CostEntry validCost = CostHelper.findValidCost(
                    EnchantmentCostRegistry.getServerRegistry().getReplicateCost().getCostForLevel(1),
                    costItems,
                    player.experienceLevel);

            boolean isBookReplicated = ReplicatedDataComponent.isReplicated(ancientBookStack);

            if(validCost != null || player.isCreative() && !isBookReplicated) {

               // player.giveExperienceLevels(-validCost.xpLevels());

                //Save old enchantment for text
                Registry<Enchantment> enchantmentRegistry = ImmersiveEnchanting.getEnchantmentRegistry(level.registryAccess());
                Enchantment oldEnchantment = enchantmentRegistry.get(AncientBook.getStoredEnchantment(ancientBookStack, level));

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




                //Get the enchantment from the registry.
                Enchantment newEnchantment = enchantmentRegistry.get(randomEnchantment.getKey());

                Component enchantmentName = newEnchantment.description().copy().withStyle(ChatFormatting.GOLD);

                Component text = Component.translatable(
                                "gui.immersiveenchanting.transmuted_to",
                                enchantmentName)
                        .withStyle(ChatFormatting.GRAY);


                player.displayClientMessage(
                        text,
                        true
                );

                player.closeContainer();
            } else {
                //If unable to enchant
                level.playSound(null, player.blockPosition(), SoundEvents.VAULT_CLOSE_SHUTTER,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
            }


        }
    }
}
