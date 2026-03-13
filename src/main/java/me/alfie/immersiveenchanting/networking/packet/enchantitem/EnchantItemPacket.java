package me.alfie.immersiveenchanting.networking.packet.enchantitem;

import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.cost.CostDefinition;
import me.alfie.immersiveenchanting.datapack.cost.CostEntry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.CostHelper;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class EnchantItemPacket {

    public final ResourceKey<Enchantment> enchantment;
    public final int enchantmentLevel;

    public EnchantItemPacket(ResourceKey<Enchantment> enchantment, int enchantmentLevel) {
        this.enchantment = enchantment;
        this.enchantmentLevel = enchantmentLevel;
    }

    public static void encode(EnchantItemPacket packet, FriendlyByteBuf buf) {
        buf.writeResourceKey(packet.enchantment);
        buf.writeInt(packet.enchantmentLevel);
    }

    public static EnchantItemPacket decode(FriendlyByteBuf buf) {
        ResourceKey<Enchantment> enchantment = buf.readResourceKey(Registries.ENCHANTMENT);
        int enchantmentLevel = buf.readInt();
        return new EnchantItemPacket(enchantment, enchantmentLevel);
    }

    public static void handle(EnchantItemPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(
                () -> exec(packet, contextSupplier.get()));
        contextSupplier.get().setPacketHandled(true);
    }

    public static void exec(EnchantItemPacket packet, NetworkEvent.Context context) {
        Player player = context.getSender();
        Level level = player.level();
        EnchantingTableMenu enchantingTableMenu = (EnchantingTableMenu) player.containerMenu;
        ItemStack itemToEnchant = enchantingTableMenu.getToolSlotItem();
        BlockPos tablePos = enchantingTableMenu.getBlockPos();


        Optional<Holder.Reference<Enchantment>> enchantmentReference = EnchantmentUtil.getEnchantmentHolder(
                level.registryAccess(), packet.enchantment);
        Holder<Enchantment> enchantmentHolder = enchantmentReference.orElseThrow(() ->
                new IllegalStateException("Enchantment not found: " + packet.enchantment));

        int playerXp = player.experienceLevel;
        ItemStack costSlotItemStack = enchantingTableMenu.getCostSlotItem();
        List<ItemStack> insertedItems = new ArrayList<>();
        insertedItems.add(costSlotItemStack);
        List<ItemStack> insertedFuels = new ArrayList<>();
        insertedFuels.add(enchantingTableMenu.getEnchantingFuelSlotItem());

        CostDefinition costNode = EnchantmentCostRegistry.getServerRegistry().getEnchantmentCost(packet.enchantment).getCostForLevel(packet.enchantmentLevel);
        CostEntry validCost = CostHelper.findValidCost(costNode, insertedItems, playerXp);

        //Check fuel
        CostDefinition enchantingFuelNode = EnchantmentCostRegistry.getServerRegistry().getEnchantingFuels().getCostForLevel(packet.enchantmentLevel);
        CostEntry validEnchantingFuel = CostHelper.findValidCost(enchantingFuelNode, insertedFuels, playerXp);

        boolean hasEnoughCost = player.isCreative()
                || (CostHelper.isCostValid(validCost)
                && CostHelper.isCostValid(validEnchantingFuel));


        if (hasEnoughCost) {
            if(player.isCreative()) {
                validCost = CostEntry.EMPTY;
                validEnchantingFuel = CostEntry.EMPTY;
            }

            assert validCost != null;
            assert validEnchantingFuel != null;
            CostHelper.deductCost(validCost, validEnchantingFuel,
                    costSlotItemStack, enchantingTableMenu.getEnchantingFuelSlotItem(),
                    player);

            //1.20.1 item.enchant() doesnt overwrite enchantments, it appends them
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemToEnchant);
            enchantments.remove(enchantmentHolder.value()); //Remove old one
            enchantments.put(enchantmentHolder.value(), packet.enchantmentLevel); //Add new one
            EnchantmentHelper.setEnchantments(enchantments, itemToEnchant);

            player.awardStat(Stats.ENCHANT_ITEM);
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, itemToEnchant, validCost.xpLevels());
            }

            boolean isHighestTier = packet.enchantmentLevel == EnchantmentCostRegistry.getServerRegistry()
                    .getEnchantmentCost(packet.enchantment)
                    .getHighestLevel();
            FxHelper.playEnchantSuccessFx(level, tablePos, isHighestTier);
        } else {
            FxHelper.playEnchantFailFx(level, tablePos);
        }
    }

}
