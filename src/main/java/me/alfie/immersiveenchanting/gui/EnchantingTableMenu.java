package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.Cost;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.EnchantingTableContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

    private List<Holder<Enchantment>> availableEnchantments = new ArrayList<>();

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
     * <p>On the server side, when a valid {@link Level} and {@link BlockPos} are
     * provided and there is an {@link EnchantingTableBlockEntity} at that
     * position, the menu's container is backed directly by that block entity's
     * persistent inventory (see {@code EnchantingTableBlockEntityMixin}). This
     * means items placed in the table stay there after the menu is closed, and
     * they can be rendered floating above the table by the block-entity
     * renderer.</p>
     *
     * <p>On the client side, where {@code level} and {@code pos} are typically
     * {@code null} when constructed from the network buffer, a plain
     * {@link SimpleContainer} is used. Vanilla slot synchronisation packets
     * still keep the GUI in sync with the server.</p>
     *
     * @param containerId     The container ID
     * @param playerInventory The player's inventory
     * @param level           The level the menu is operating in
     * @param pos             The position of the enchanting table block
     */
    public EnchantingTableMenu(int containerId, Inventory playerInventory, @Nullable Level level, @Nullable BlockPos pos) {
        super(ModMenus.ENCHANTING_TABLE_MENU.get(), containerId);
        this.blockPos = pos;
        this.level = level;
        this.access = ContainerLevelAccess.create(level, pos);

        // On the server, back the container with the BE so items persist
        // between menu opens. On the client (or when there's no BE at the
        // given position) fall back to a plain SimpleContainer.
        Container chosenContainer = null;
        if (level != null && !level.isClientSide() && pos != null) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EnchantingTableBlockEntity enchantingBE) {
                chosenContainer = new EnchantingTableContainer(enchantingBE);
            }
        }
        this.container = chosenContainer != null ? chosenContainer : new SimpleContainer(3);

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
            slot.setChanged(); // notify the BE-backed container so the renderer updates
        } else if (i == Slots.ENCHANTING_FUEL.id()) {
            if (!this.moveItemStackTo(stack, 3, 39, true)) return ItemStack.EMPTY;
            slot.setChanged();
        } else if (i == Slots.COST.id()) {
            if (!this.moveItemStackTo(stack, 3, 39, true)) return ItemStack.EMPTY;
            slot.setChanged();
        } else {
            CostRegistry registry = CostRegistry.client();
            if(level != null && !level.isClientSide()) {
                registry = CostRegistry.server();
            }
            List<Cost> enchantingFuels = registry.get(CostRegistry.ENCHANTING_FUELS)
                    .levelCosts().getAllLevels();

            Set<Item> enchantingFuelItems = new HashSet<>();
            for(Cost enchantmentCost : enchantingFuels) {
                for(ItemStack fuelStack : enchantmentCost.getItemStacks()) {
                    enchantingFuelItems.add(fuelStack.getItem());
                }
            }

            //Quick move into menu
            if(stack.getItem().isEnchantable(stack) || stack.is(ModItems.ANCIENT_BOOK.get())) {
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
     * <p>If the container is backed by an {@link EnchantingTableBlockEntity}
     * (server side), items are intentionally left in the table — the player
     * can come back later and continue. The block entity's inventory is
     * dropped only when the block itself is destroyed (handled by
     * {@code EnchantingTableBreakHandler}).</p>
     *
     * <p>If the container is a plain {@link SimpleContainer} (fallback path,
     * for example if no BE was found at the given position), the items are
     * returned to the player to avoid losing them.</p>
     *
     * @param player The player closing the menu
     */
    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);

        if (player.level().isClientSide()) return;

        // BE-backed container: leave the items in the block. They persist in
        // NBT and will be visible to the next player who opens the table.
        if (this.container instanceof EnchantingTableContainer) {
            return;
        }

        // Fallback path: return items to the player so they aren't lost.
        for (int i = 0; i < this.container.getContainerSize(); i++) {
            ItemStack stack = this.container.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stack);
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

    public void setAvailableEnchantments(List<Holder<Enchantment>> availableEnchantments) {
        this.availableEnchantments.clear();
        this.availableEnchantments.addAll(availableEnchantments);
    }

    public List<Holder<Enchantment>> getAvailableEnchantments() {
        return availableEnchantments;
    }

    public boolean isEnchantmentAvailable(Holder<Enchantment> enchantmentHolder) {
        return getAvailableEnchantments().contains(enchantmentHolder);
    }
}
