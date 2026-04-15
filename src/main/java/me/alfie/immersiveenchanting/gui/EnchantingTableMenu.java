package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.Cost;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Server-authoritative menu for the custom enchanting table.
 *
 * <p>This menu handles:
 * <ul>
 *     <li>Inventory slot layout (tool, fuel, cost, and player inventory)</li>
 *     <li>Detection of nearby chiseled bookshelves</li>
 *     <li>Extraction of "ancient book" enchantments from those shelves</li>
 *     <li>Maintaining a list of available enchantments based on environment</li>
 * </ul>
 *
 * <p><b>Client/Server Notes:</b>
 * <ul>
 *     <li>Enchantment discovery logic runs only on the server</li>
 * </ul>
 */
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
    private final Level level;
    /**
     * Client-side constructor used when the menu is opened.
     *
     * <p>Reads the {@link BlockPos} from the network buffer and resolves the level
     * from the player instance.</p>
     *
     * @param containerId     The container ID assigned by Minecraft
     * @param playerInventory The player's inventory
     * @param buf             Network buffer containing synced data (block position)
     */
    public EnchantingTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level(), buf.readBlockPos());
    }

    /**
     * Primary constructor for the enchanting table menu.
     *
     * <p>Initializes the container, slot layout, and scans for available enchantments
     * if running on the server.</p>
     *
     * @param containerId     The container ID
     * @param playerInventory The player's inventory
     * @param level           The level the menu is operating in
     * @param pos             The position of the enchanting table block
     */
    public EnchantingTableMenu(int containerId, Inventory playerInventory, @Nullable Level level, @Nullable BlockPos pos) {
        super(ModMenus.ENCHANTING_TABLE_MENU.get(), containerId);
        this.container = new SimpleContainer(3);
        this.blockPos = pos;
        this.level = level;
        this.access = ContainerLevelAccess.create(level, pos);
        buildSlots(playerInventory);
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    /**
     * Builds and registers all menu slots.
     *
     * <p>Includes:
     * <ul>
     *     <li>Tool slot (1 item max)</li>
     *     <li>Enchanting fuel slot</li>
     *     <li>Cost slot</li>
     *     <li>Player inventory and hotbar</li>
     * </ul>
     *
     * @param playerInventory The player's inventory
     */
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

        //Player inventory
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

    /**
     * Handles shift-click item transfers between the menu and player inventory.
     *
     * <p>Rules:
     * <ul>
     *     <li>Tools go into the tool slot</li>
     *     <li>Valid enchanting fuels go into the fuel slot</li>
     *     <li>All other items go into the cost slot</li>
     * </ul>
     *
     * @param player The player interacting with the menu
     * @param i      The slot index
     * @return The moved {@link ItemStack}, or {@link ItemStack#EMPTY} if transfer failed
     */
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int i) {
        ItemStack stack = ItemStack.EMPTY;
        if(level == null) return stack;

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
            List<Cost> enchantingFuels = CostRegistry.server().get(CostRegistry.ENCHANTING_FUELS)
                    .levelCosts().getAllLevels();

            Set<Item> enchantingFuelItems = new HashSet<>();
            for(Cost enchantmentCost : enchantingFuels) {
                for(ItemStack fuelStack : enchantmentCost.getItemStacks()) {
                    enchantingFuelItems.add(fuelStack.getItem());
                }
            }

            //Quick move into menu
            if(EnchantmentHelper.canStoreEnchantments(stack) || stack.is(ModItems.ANCIENT_BOOK.get())) {
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

    /**
     * Checks whether the player can still interact with this menu.
     *
     * <p>Ensures the player is within range of the enchanting table block.</p>
     *
     * @param player The player
     * @return {@code true} if the menu is still valid
     */
    @Override
    public boolean stillValid(@NotNull Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
    }

    /**
     * Called when the menu is closed.
     *
     * <p>On the server:
     * <ul>
     *     <li>All items in the internal container are returned to the player</li>
     *     <li>If the inventory is full, items are dropped</li>
     * </ul>
     *
     * @param player The player closing the menu
     */
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

    /**
     * Gets a slot by enum type.
     *
     * @param slot The slot enum
     * @return The corresponding {@link Slot}
     */
    public Slot getSlot(Slots slot) {
        return this.getSlot(slot.id());
    }

    /**
     * @return The tool slot
     */
    public Slot getToolSlot() {
        return this.getSlot(Slots.TOOL.id());
    }

    /**
     * @return The enchanting fuel slot
     */
    public Slot getFuelSlot() {
        return this.getSlot(Slots.ENCHANTING_FUEL.id());
    }

    /**
     * @return The cost slot
     */
    public Slot getCostSlot() {
        return this.getSlot(Slots.COST.id());
    }
}
