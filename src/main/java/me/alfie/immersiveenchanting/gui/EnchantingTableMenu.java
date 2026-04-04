package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.datapack.EnchantmentCost;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EnchantingTableMenu extends AbstractContainerMenu {

    public enum Slots {
        TOOL(0),
        ENCHANTING_FUEL(1),
        COST(2);

        private final int id;

        Slots(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }
    }

    private final BlockPos blockPos;
    private final ContainerLevelAccess access;
    private final Container container;

    public EnchantingTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level(), buf.readBlockPos());
    }

    public EnchantingTableMenu(int containerId, Inventory playerInventory, Level level, BlockPos pos) {
        super(ModMenus.ENCHANTING_TABLE_MENU.get(), containerId);
        this.container = new SimpleContainer(3);
        this.blockPos = pos;
        this.access = ContainerLevelAccess.create(level, pos);
        buildSlots(playerInventory);
    }

    private void buildSlots(Inventory playerInventory) {
        //Tool slot
        this.addSlot(new Slot(this.container, Slots.TOOL.id(), 233, 141) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        //Enchanting fuel slot
        this.addSlot(new Slot(this.container, Slots.ENCHANTING_FUEL.id(), 233, 199));

        //Cost slot
        this.addSlot(new Slot(this.container, Slots.COST.id(), 233, 170));

        //Add player inventory slots
        int startX = 17;
        int startY = 140;

        //Player inventory 3 rows of 9
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, startX + col * 18, startY + row * 18));
            }
        }

        //Hotbar slots
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, startX + col * 18, startY + 58));
        }
    }

    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int i) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = getSlot(i);

        if(!slot.hasItem()) return stack;
        stack = slot.getItem();

        //Quick move into inventory
        if(i == Slots.TOOL.id()) {
            if(!this.moveItemStackTo(stack, 3, 39, true)) return ItemStack.EMPTY;
        } else if (i == Slots.ENCHANTING_FUEL.id()) {
            if (!this.moveItemStackTo(stack, 3, 39, true)) return ItemStack.EMPTY;
        } else if (i == Slots.COST.id()) {
            if (!this.moveItemStackTo(stack, 3, 39, true)) return ItemStack.EMPTY;
        } else {
            List<EnchantmentCost> enchantingFuels = EnchantmentCostRegistry.get(EnchantmentCostRegistry.ENCHANTING_FUELS)
                    .levelCosts().getAllLevels();

            Set<Item> enchantingFuelItems = new HashSet<>();
            for(EnchantmentCost enchantmentCost : enchantingFuels) {
                for(ItemStack fuelStack : enchantmentCost.getItemStacks()) {
                    enchantingFuelItems.add(fuelStack.getItem());
                }
            }

            //Quick move into menu
            if(stack.isEnchantable()) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
            } else if(enchantingFuelItems.contains(stack.getItem())) {
                if (!this.moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(stack, 2, 3, false)) return ItemStack.EMPTY;
            }

            if(stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return stack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);

        if (!player.level().isClientSide()) {
            for (int i = 0; i < this.container.getContainerSize(); i++) {
                ItemStack stack = this.container.removeItemNoUpdate(i); //remove without triggering slot update
                if (!stack.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(stack); //return to inventory, drop if full
                }
            }
        }
    }

    public Slot getSlot(Slots slot) {
        return this.getSlot(slot.id());
    }

    public Slot getToolSlot() {
        return this.getSlot(Slots.TOOL.id());
    }

    public Slot getFuelSlot() {
        return this.getSlot(Slots.ENCHANTING_FUEL.id());
    }

    public Slot getCostSlot() {
        return this.getSlot(Slots.COST.id());
    }
}
