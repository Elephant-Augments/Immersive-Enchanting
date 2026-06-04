package me.alfie.immersiveenchanting.mixin;

import me.alfie.immersiveenchanting.util.IEnchantingTableInventory;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentTableBlockEntity.class)
public abstract class EnchantingTableBlockEntityMixin extends BlockEntity implements IEnchantingTableInventory {

    // Required by the BlockEntity superclass constructor; never actually called
    // because Mixin doesn't add new constructors. Present only to satisfy javac.
    private EnchantingTableBlockEntityMixin() {
        super(null, null, null);
    }

    @Unique
    private final NonNullList<ItemStack> immersive$items =
            NonNullList.withSize(IMMERSIVE_INVENTORY_SIZE, ItemStack.EMPTY);

    @Override
    public NonNullList<ItemStack> immersive$getItems() {
        return this.immersive$items;
    }

    @Override
    public ItemStack immersive$getItem(int slot) {
        if (slot < 0 || slot >= IMMERSIVE_INVENTORY_SIZE) return ItemStack.EMPTY;
        return this.immersive$items.get(slot);
    }

    @Override
    public void immersive$setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= IMMERSIVE_INVENTORY_SIZE) return;
        this.immersive$items.set(slot, stack);
    }

    @Override
    public void immersive$setChangedAndSync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    // ---- NBT persistence ----
    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void immersive$saveAdditional(CompoundTag tag, CallbackInfo ci) {
        ContainerHelper.saveAllItems(tag, this.immersive$items, true);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void immersive$loadAdditional(CompoundTag tag, CallbackInfo ci) {
        // Reset to empty before loading so removed slots clear properly.
        for (int i = 0; i < IMMERSIVE_INVENTORY_SIZE; i++) this.immersive$items.set(i, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.immersive$items);
    }

    // ---- Client sync ----
    // Vanilla EnchantingTableBlockEntity does not override getUpdatePacket /
    // getUpdateTag. By default these return null on a base BlockEntity, so the
    // client never receives our items. We override both here so the items are
    // sent on chunk load and on every block update we trigger via setChanged.

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        // saveWithoutMetadata writes the BE's full data (vanilla's customName,
        // plus our injected items list) without the position/type metadata
        // that's already known on the client side.
        return this.saveWithoutMetadata();
    }


}
