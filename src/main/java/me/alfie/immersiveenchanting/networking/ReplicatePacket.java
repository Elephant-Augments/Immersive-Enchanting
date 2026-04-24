package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ReplicatePacket() implements ModNetworkPacket<ReplicatePacket> {

    public static final Type<@NotNull ReplicatePacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "replicate"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReplicatePacket> STREAM_CODEC = StreamCodec.unit(new ReplicatePacket());

    @Override
    public Type<@NotNull ReplicatePacket> typeId() {
        return TYPE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ReplicatePacket> codec() {
        return STREAM_CODEC;
    }

    @Override
    public void exec(ReplicatePacket packet, IPayloadContext context) {
        if(!ServerConfig.isAllowReplicate()) return;

        Player player = context.player();
        Level level = player.level();
        if(!(player.containerMenu instanceof EnchantingTableMenu menu)) return;

        if(EnchantmentUtil.canReplicate(menu, context)) {
            EnchantmentUtil.deductValidCost(menu, CostRegistry.TRANSMUTE, 1, player, CostRegistry.server());

            ItemStack oldStack = menu.getToolSlot().getItem().copyAndClear();
            ItemStack newStack = oldStack.copy();
            EnchantmentUtil.setReplicated(newStack);

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
