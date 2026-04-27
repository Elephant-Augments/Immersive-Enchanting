package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ReplicatePacket() implements ModNetworkPacket {

    public static final PacketCodec<ReplicatePacket> CODEC = new PacketCodec<>() {
        @Override
        public void encode(ReplicatePacket packet, FriendlyByteBuf buf) {

        }

        @Override
        public ReplicatePacket decode(FriendlyByteBuf buf) {
            return new ReplicatePacket();
        }
    };

    @Override
    public void exec(NetworkEvent.Context context) {
        if(!context.getDirection().getReceptionSide().isServer()) return;
        if(!ServerConfig.isAllowReplicate()) return;

        Player player = context.getSender();
        Level level = player.level();
        if(!(player.containerMenu instanceof EnchantingTableMenu menu)) return;

        if(EnchantmentUtil.canReplicate(menu, context)) {
            EnchantmentUtil.deductValidCost(menu, CostRegistry.TRANSMUTE, 1, player, CostRegistry.server());

            ItemStack oldStack = menu.getToolSlot().getItem().copyAndClear();
            ItemStack newStack = oldStack.copy();
            EnchantmentUtil.setReplicatedNbtTag(newStack);

            BlockPos tablePos = menu.getBlockPos();
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

            FxHelper.playReplicate((ServerLevel) level, tablePos);
            player.closeContainer();
        }
    }
}
