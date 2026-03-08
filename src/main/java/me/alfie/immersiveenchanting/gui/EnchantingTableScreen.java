package me.alfie.immersiveenchanting.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.cost.EnchantmentCost;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.gui.core.*;
import me.alfie.immersiveenchanting.gui.core.booktab.BookTab;
import me.alfie.immersiveenchanting.gui.core.booktab.FilterCheckbox;
import me.alfie.immersiveenchanting.gui.enchanting.EnchantingNode;
import me.alfie.immersiveenchanting.gui.enchanting.EnchantingNodeTooltip;
import me.alfie.immersiveenchanting.gui.replicate.ReplicateNode;
import me.alfie.immersiveenchanting.gui.replicate.ReplicateNodeTooltip;
import me.alfie.immersiveenchanting.gui.transmute.TransmuteNode;
import me.alfie.immersiveenchanting.gui.transmute.TransmuteNodeTooltip;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.networking.packet.enchantitem.EnchantItemPacket;
import me.alfie.immersiveenchanting.networking.packet.removeenchantment.RemoveEnchantmentPacket;
import me.alfie.immersiveenchanting.networking.packet.replicatebookpacket.ReplicateBookPacket;
import me.alfie.immersiveenchanting.networking.packet.transmutebookpacket.TransmuteBookPacket;
import me.alfie.immersiveenchanting.networking.packet.updatetoolslot.UpdateToolSlotPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.List;

public class EnchantingTableScreen extends AbstractContainerScreen<EnchantingTableMenu> {

    public static final ResourceLocation ENCHANTING_TABLE_BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID,
            "textures/gui/container/enchanting_table.png");
    public static final ResourceLocation TILE_TEXTURE = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID,
            "textures/gui/container/enchanting_background_tile.png");
    public static final ResourceLocation BOOK_OPEN_TEXTURE = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID,
            "textures/gui/container/book_open_shadow.png");
    public static final ResourceLocation ENCHANTING_TABLE_TOP_TEXTURE = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID,
            "textures/gui/container/enchanting_table_top.png");
    public static final ResourceLocation BOOK_CLOSED_TEXTURE = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID,
            "textures/gui/container/book_closed.png");
    public static final ResourceLocation LEVEL_SPRITE = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID,
            "textures/gui/sprites/level_10.png");
    public static final ResourceLocation XP_LEVEL_SPRITE = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID,
            "textures/gui/sprites/xp_level.png");

    public List<NodeBranch> branches = new ArrayList<>();




    public List<Node> getRenderedNodes() {
        return rendered_nodes;
    }

    final List<Node> rendered_nodes = new ArrayList<>();

    private final boolean croppingEnabled = true; //Whether to crop the canvas outside of viewport - false for debugging.
    public final Player player;
    private final int VIRTUAL_SLOT_DIMENSIONS = 16;



    private float currentBackgroundBrightness = 1f;
    private Node hoveredNode;
    private Node lastHoveredNode;
    private boolean lockHover;
    private NodeTooltip nodeTooltip;
    private ItemStack lastStack = ItemStack.EMPTY; //Handling which tool in slot
    private Vector2i virtualSlotPos;

    ScrollableCanvas canvas;

    private Node nodeHeld;
    private long holdStartTime;
    public final long HOLD_THRESHOLD = 1000;

    private Vector2i tabTopLeft = new Vector2i(200, 126);
    private int tabWidth = 20;
    private int tabHeight = 25;

    public BookTab bookTab;
    private ItemStack tabIcon = new ItemStack(ModItems.ANCIENT_BOOK.get(), 1);

    public EnchantingTableScreen(EnchantingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        canvas = new ScrollableCanvas(this);

        this.titleLabelX = 10;
        this.inventoryLabelX = 10;
        this.imageHeight = 222;
        this.imageWidth = 256;

        // Center the canvas inside the viewport at start
        canvas.setScrollX((canvas.getWidth() / 2.0) - (canvas.VIEWPORT_WIDTH / 2.0));
        canvas.setScrollY((canvas.getHeight() / 2.0) - (canvas.VIEWPORT_HEIGHT / 2.0));

        //No branch shenanigans here, do it in init() pls <3
        this.player = playerInventory.player;
        bookTab = new BookTab(this);
    }

    public ScrollableCanvas getCanvas() {
        return this.canvas;
    }

    @Override //Init code when GUI is created.
    public void init() {
        super.init();

        initializeScreen();
        onToolSlotChanged(); //Updates if screen size is changed while screen open
    }

    private void initializeScreen() {
        canvas.calculateSize();

        //Prevent tearing
        branches.clear();
        rendered_nodes.clear();
    }

    /**
     * Call when tool slot changes. Refresh the screen.
     */
    public void onToolSlotChanged() {
        player.playSound(SoundEvents.BOOK_PAGE_TURN);

        //Clear all nodes
        branches.clear();
        rendered_nodes.clear();
        setLockHover(false);

        if (menu.isToolSlotEmpty()) {
            initializeScreen();
            canvas.setDraggingEnabled(false);
        } else {
            canvas.setDraggingEnabled(true);
        }

        if(menu.getToolSlotItem().is(ModItems.ANCIENT_BOOK.get())) {
            branches = BranchFactory.buildAncientBookBranch(menu.getToolSlotItem(), this);
        } else {
            branches = BranchFactory.buildEnchantingNodeBranches(menu.getToolSlotItem(),this);
        }

        NodeBranch.calculateNodeAnglesAndStep(this);
        //Place nodes after size change
        for (NodeBranch branch : branches) {
            branch.placeNodesAlongLine();
            for (Node node : branch.getNodes()) {
                node.setScale(EnchantingNode.globalScale);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        //Mouse held
        if(nodeHeld != null) {
            mouseHold(mouseX, mouseY);
        }

        //Render the item stack for enchanting node tooltips when hovered.
        if (nodeTooltip != null) {
            if (mouseX >= nodeTooltip.getCostStackPos().x
                    && mouseX < nodeTooltip.getCostStackPos().x + 16
                    && mouseY >= nodeTooltip.getCostStackPos().y
                    && mouseY < nodeTooltip.getCostStackPos().y + 16) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 500);

                List<Component> lines = nodeTooltip.getCurrentRenderedCost().asItemStack().getTooltipLines(
                        Item.TooltipContext.EMPTY, null, TooltipFlag.ADVANCED
                );
                if(!(Objects.equals(nodeTooltip.stackDescriptionComponents.get(0), Component.empty()))) {
                    lines.add(1, nodeTooltip.stackDescriptionComponents.getFirst());
                }

                guiGraphics.renderTooltip(font,
                        lines,
                        nodeTooltip.getCurrentRenderedCost().asItemStack().getTooltipImage(),
                        nodeTooltip.getCurrentRenderedCost().asItemStack(),
                        mouseX,
                        mouseY);

                guiGraphics.pose().popPose();
            }
        }

        //Render the item in the enchanting table when hovered
        if(!menu.getToolSlotItem().is(Items.AIR) && !isLockHover()) {
            if(canvas.isMouseOverBoundingBox(virtualSlotPos, VIRTUAL_SLOT_DIMENSIONS, VIRTUAL_SLOT_DIMENSIONS, mouseX, mouseY)) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 500);
                guiGraphics.renderTooltip(font,
                        menu.getToolSlotItem(),
                        mouseX, mouseY);
                guiGraphics.pose().popPose();
            }
        }

        if(isMouseOverTab(mouseX, mouseY)) {
            String key = "";
            if(canvas.getCanvasState().equals(CanvasState.ENCHANTING)) {
                key = "gui.immersiveenchanting.switch_book_tab";
                tabIcon = new ItemStack(ModItems.ANCIENT_BOOK.get(), 1);
            } else if (canvas.getCanvasState().equals(CanvasState.BOOKS)) {
                key = "gui.immersiveenchanting.switch_enchanting_tab";
                tabIcon = new ItemStack(Items.ENCHANTING_TABLE, 1);
            }

            //Draw tooltip
            guiGraphics.renderTooltip(
                    font, Component.translatable(key), mouseX, mouseY
            );
        }

        //Draw item tooltips
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    public boolean isMouseOverTab(int mouseX, int mouseY) {
        return isMouseOver(mouseX, mouseY,
                getGuiLeft() + tabTopLeft.x,
                getGuiTop() + tabTopLeft.y,
                tabWidth,
                tabHeight);
    }

    /**
     * Returns true if mouse is over the bounds given. Note: this should only be used for static elements on the screen.
     * <br>For scrollable canvas elements, use ScrollableCanvas.isMouseOverBoundingBox
     * @param mouseX
     * @param mouseY
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        //Inventory labels not required for this GUI, method has to be included though.
    }

    /**
     * Main render code.
     *
     * @param guiGraphics
     * @param partialTick
     * @param mouseX
     * @param mouseY
     */
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        if(canvas.getCanvasState().equals(CanvasState.ENCHANTING)) {
            renderEnchantingScreen(guiGraphics, mouseX, mouseY);
        } else if(canvas.getCanvasState().equals(CanvasState.BOOKS)) {
            renderBooksScreen(guiGraphics, mouseX, mouseY);
        }



        //Disable scissor after drawing
        RenderSystem.disableScissor();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(ENCHANTING_TABLE_BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        RenderSystem.disableBlend();

        //Draw tab icon
        guiGraphics.renderItem(tabIcon, getGuiLeft()+202, getGuiTop()+132);
    }

    private void renderBooksScreen(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        //Fade the background and nodes darker if hovering
        guiGraphics.setColor(0.5F, 0.5F, 0.5F, 1f);
        canvas.renderTiledBg(guiGraphics);

        bookTab.render(guiGraphics);
    }

    private void renderEnchantingScreen(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        //Fade the background and nodes darker if hovering
        float targetBrightness = isHoveringAnyNode() ? 0.15f : 1f;
        float fadeSpeed = 0.3f;
        currentBackgroundBrightness += (targetBrightness - currentBackgroundBrightness) * fadeSpeed;
        guiGraphics.setColor(currentBackgroundBrightness, currentBackgroundBrightness, currentBackgroundBrightness, 1f);

        //Darken background if tool slot is empty
        if (menu.isToolSlotEmpty()) {
            guiGraphics.setColor(0.5F, 0.5F, 0.5F, 1f);
        }

        canvas.renderTiledBg(guiGraphics);

        renderBranchConnections(guiGraphics);
        renderCentralSprites(guiGraphics);
        renderNodes(guiGraphics);

        Node nodeToRender;
        if (isLockHover()) {
            nodeToRender = lastHoveredNode;
        } else {
            setHoveredNode(mouseX, mouseY); //Update hovered node
            nodeToRender = getHoveredNode();
        }

        if (nodeToRender != null) {
            renderNodeTooltip(nodeToRender, guiGraphics);
            renderHoveredNode(nodeToRender, guiGraphics);
        } else {
            nodeTooltip = null;
        }
    }

    private boolean isHoveringAnyNode() {
        return hoveredNode != null;
    }


    private void renderBranchConnections(GuiGraphics guiGraphics) {
        for (NodeBranch branch : branches) {
            guiGraphics.setColor(currentBackgroundBrightness, currentBackgroundBrightness, currentBackgroundBrightness, 1f);

            //Don't darken the hovered node branch.
            if (branch.getNodes().contains(hoveredNode)) {
                guiGraphics.setColor(1f, 1f, 1f, 1f);
            }

            //branch.drawNodeConnections(guiGraphics);
            if (!branch.hasCalculatedConnections()) {
                branch.calculateNodeConnections();
            }

            branch.renderPrecomputedConnection(guiGraphics, (int) canvas.getScrollX(), (int) canvas.getScrollY());

            //Reset color after darkening
            guiGraphics.setColor(1f, 1f, 1f, 1f);
        }
    }

    /**
     * Render the enchanting table, book, and item in the centre of the screen.
     *
     * @param guiGraphics
     */
    private void renderCentralSprites(GuiGraphics guiGraphics) {
        //Add background book and virtual slot
        guiGraphics.blit(
                ENCHANTING_TABLE_TOP_TEXTURE,
                canvas.getCenterPos(32, 32).x - (int) canvas.getScrollX(),
                canvas.getCenterPos(32, 32).y - (int) canvas.getScrollY(),
                0f, 0f, 32, 32,
                32, 32
        );
        virtualSlotPos = canvas.getCenterPos(VIRTUAL_SLOT_DIMENSIONS, VIRTUAL_SLOT_DIMENSIONS);

        if (this.menu.getSlot(0).getItem().isEmpty()) {
            guiGraphics.blit(
                    BOOK_CLOSED_TEXTURE,
                    canvas.getCenterPos(32, 32).x - (int) canvas.getScrollX(),
                    canvas.getCenterPos(32, 32).y - (int) canvas.getScrollY(),
                    0f, 0f, 32, 32,
                    32, 32
            );
        } else {
            guiGraphics.blit(
                    BOOK_OPEN_TEXTURE,
                    canvas.getCenterPos(32, 32).x - (int) canvas.getScrollX(),
                    canvas.getCenterPos(32, 32).y - (int) canvas.getScrollY(),
                    0f, 0f, 32, 32,
                    32, 32
            );
        }


        //Render the item in slot0 (tool slot)
        ItemStack stack = this.menu.getSlot(0).getItem(); //to get the ItemStack
        guiGraphics.renderItem(stack, canvas.getCenterPos(16, 16).x - (int) canvas.getScrollX(),
                canvas.getCenterPos(16, 16).y - (int) canvas.getScrollY());
    }

    /**
     * Render nodes (excluding the current hovered node).
     *
     * @param guiGraphics
     */
    private void renderNodes(GuiGraphics guiGraphics) {
        for (Node node : rendered_nodes) {
            guiGraphics.setColor(currentBackgroundBrightness, currentBackgroundBrightness, currentBackgroundBrightness, 1f);
            node.render(guiGraphics, canvas);

            node.setScale(EnchantingNode.globalScale);

            //Reset color after darkening
            guiGraphics.setColor(1f, 1f, 1f, 1f);
        }
    }

    public boolean isLockHover() {
        return lockHover;
    }

    /**
     * Set which node is currently being hovered over.
     * @param mouseX
     * @param mouseY
     */
    private void setHoveredNode(double mouseX, double mouseY) {
        for (Node node : rendered_nodes) {
            if (node.isMouseOver(canvas, mouseX, mouseY)) {
                hoveredNode = node;
                return;
            }
        }
        hoveredNode = null;
        nodeHeld = null;
    }

    private Node getHoveredNode() {
        return hoveredNode;
    }

    private void renderNodeTooltip(Node node, GuiGraphics guiGraphics) {
        //Create new node tooltip
        if (!node.equals(lastHoveredNode) || nodeTooltip == null) {

            if(node instanceof EnchantingNode enchantingNode) {
                nodeTooltip = new EnchantingNodeTooltip(
                        enchantingNode,
                        this,
                        EnchantmentCost.getRenderableAnyOfCosts(
                                EnchantmentCostRegistry.getClientRegistry()
                                        .getEnchantmentCost(enchantingNode.getEnchantment())
                                        .getCostForLevel(enchantingNode.getEnchantmentLevel())
                        ));
            }

            else if (node instanceof TransmuteNode transmuteNode) {
                nodeTooltip = new TransmuteNodeTooltip(
                        transmuteNode,
                        this,
                        EnchantmentCost.getRenderableAnyOfCosts(
                                EnchantmentCostRegistry.getClientRegistry()
                                        .getInternalRegistry()
                                        .get(EnchantmentCostRegistry.InternalCosts.TRANSMUTE)
                                        .getCostForLevel(1)));
            }

            else if (node instanceof ReplicateNode replicateNode) {
                nodeTooltip = new ReplicateNodeTooltip(
                        replicateNode,
                        this,
                        EnchantmentCost.getRenderableAnyOfCosts(
                                EnchantmentCostRegistry.getClientRegistry()
                                        .getInternalRegistry()
                                        .get(EnchantmentCostRegistry.InternalCosts.REPLICATE)
                                        .getCostForLevel(1)));
            }

            float pitch = 1;
            if(node instanceof EnchantingNode enchantingNode) {
                int highestLevel = EnchantmentCostRegistry.getClientRegistry().getEnchantmentCost(enchantingNode.getEnchantment()).getHighestLevel();
                int thisLevel = enchantingNode.getEnchantmentLevel();

                if(thisLevel == highestLevel) {
                    pitch = 2;
                    player.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 1f, 2);
                }
            }

            player.playSound(SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED, 1f, pitch);
            lastHoveredNode = node;
        }

        //Render it
        if (nodeTooltip != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200);
            nodeTooltip.render(guiGraphics);
            guiGraphics.pose().popPose();
        }

    }

    /**
     * Render the node that is currently hovered over.
     * @param guiGraphics
     */
    private void renderHoveredNode(Node node, GuiGraphics guiGraphics) {
        //Render the hovered node.
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 500);
        node.setScale(1f);
        node.render(guiGraphics, canvas);
        guiGraphics.pose().popPose();
    }

    private void setLockHover(boolean lockHover) {
        this.lockHover = lockHover;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            //Show book screen
            if(isMouseOverTab((int) mouseX, (int) mouseY)) {
                player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.3f, 1f);
                if(canvas.getCanvasState().equals(CanvasState.ENCHANTING)) {
                    canvas.setCanvasState(CanvasState.BOOKS);
                } else if(canvas.getCanvasState().equals(CanvasState.BOOKS)) {
                    canvas.setCanvasState(CanvasState.ENCHANTING);
                }
            }

            if(canvas.getCanvasState().equals(CanvasState.BOOKS)) {

                //Toggle filter checkbox
                for(FilterCheckbox filterCheckbox : bookTab.filterCheckboxes) {
                    if(filterCheckbox.isMouseOver((int) mouseX, (int) mouseY)) {
                        filterCheckbox.toggleEnabled();
                    }
                }

                if(bookTab.scrollbar.isMouseOver((int) mouseX, (int) mouseY)) {
                    bookTab.scrollbar.isMouseDraggingScroller = true;
                    return true;
                }
            }

            ItemStack carriedStack = menu.getCarried();
            if (canvas.isDraggingEnabled()) {

                // 1. Check if hovered node was clicked
                if (hoveredNode != null) {
                    if (hoveredNode.isMouseOver(canvas, mouseX, mouseY)) {
                        this.onNodeClicked(hoveredNode);
                        return true;
                    }
                }


                // Check if the centre icon was clicked
                if (canvas.isMouseOverBoundingBox(virtualSlotPos, VIRTUAL_SLOT_DIMENSIONS, VIRTUAL_SLOT_DIMENSIONS, mouseX, mouseY)
                        && carriedStack.isEmpty() && !isLockHover()) {
                    PacketDistributor.sendToServer(
                            new UpdateToolSlotPacket(UpdateToolSlotPacket.MODE.TAKE.ordinal())
                    );
                    return true;
                }

                // 2. Start a drag if no node clicked
                if(canvas.startDrag(mouseX, mouseY)) return true;

            } else {
                //Send packet to place carried item in slot.
                //Check if mouse if over the virtual slot
                if (canvas.isMouseOverBoundingBox(virtualSlotPos, VIRTUAL_SLOT_DIMENSIONS, VIRTUAL_SLOT_DIMENSIONS, mouseX, mouseY)
                        && !carriedStack.isEmpty()) {
                    PacketDistributor.sendToServer(
                            new UpdateToolSlotPacket(UpdateToolSlotPacket.MODE.PLACE.ordinal())
                    );
                    return true;
                }
            }

        }

        if (button == 1) {
            if (isHoveringAnyNode()) { //Right click a node to lock it, right click anywhere to escape.
                setLockHover(!isLockHover());

                if (isLockHover()) {
                    player.playSound(SoundEvents.DISPENSER_FAIL, 1f, 2f);
                }

                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if(canvas.getCanvasState().equals(CanvasState.ENCHANTING)) {
            canvas.drag(mouseX, mouseY, button);
        } else if(canvas.getCanvasState().equals(CanvasState.BOOKS)) {
            if(bookTab.scrollbar.isMouseDraggingScroller) {
                bookTab.scrollbar.updateScrollFromMouse((int) mouseY);
                return true;
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        nodeHeld = null;
        holdStartTime = 0;

        canvas.stopDrag(button);

        bookTab.scrollbar.isMouseDraggingScroller = false;
        return super.mouseReleased(mouseX, mouseY, button);

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        bookTab.scrollbar.incrementScrollIndex((int) -scrollY);

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public void mouseHold(double mouseX, double mouseY) {
        long heldTime = getMouseHeldTime();

        if(nodeHeld instanceof EnchantingNode enchantingNode && heldTime > HOLD_THRESHOLD && ServerConfig.isEnchantmentRemovalAllowed()) {
            int enchantmentLevel = enchantingNode.getEnchantmentLevel();
            int highestUnlockedLevel = menu.getToolSlotItem().getEnchantmentLevel(enchantingNode.getEnchantmentHolder());
            if (enchantmentLevel == highestUnlockedLevel) {
                nodeHeld = null;
                PacketDistributor.sendToServer(new RemoveEnchantmentPacket(
                        enchantingNode.getEnchantmentHolder().getKey(),
                        enchantingNode.getEnchantmentLevel())
                );
            }
        }

    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(canvas.getCanvasState().equals(CanvasState.BOOKS)) {
            if(keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                bookTab.searchbar.removeCharFromSearch();
            }

            if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
                return super.keyPressed(keyCode, scanCode, modifiers);
            } else {
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if(canvas.getCanvasState().equals(CanvasState.BOOKS)) {
            bookTab.searchbar.addCharToSearch(codePoint);
        }

        return super.charTyped(codePoint, modifiers);
    }

    /**
     * Detect if item changes in tool slot.
     */
    @Override
    public void containerTick() {
        super.containerTick();

        ItemStack current_item = this.menu.getToolSlotItem();
        if (!ItemStack.isSameItemSameComponents(current_item, lastStack)) {
            this.lastStack = current_item.copy();
            onToolSlotChanged();
        }
    }

    /**
     * Handle left-click on node.
     *
     * @param node
     */
    private void onNodeClicked(Node node) {

        if(node instanceof EnchantingNode enchantingNode) {
            if (!enchantingNode.isObtained() && enchantingNode.isBranchUnlocked) {
                //Give server ResourceKey<Enchantment>.location().toString()
                //Server uses RESOURCE_KEY_MAP.get() to find the corresponding ResourceKey
                //Server enchants tool, client side cannot do it.
                PacketDistributor.sendToServer(new EnchantItemPacket(
                        enchantingNode.getEnchantmentHolder().getKey(),
                        enchantingNode.getEnchantmentLevel())
                );
            } else if (enchantingNode.isObtained() && enchantingNode.isBranchUnlocked) {
                nodeHeld = node;
                holdStartTime = System.currentTimeMillis();
            }
        }

        else if (node instanceof TransmuteNode transmuteNode) {
            if(transmuteNode.canTransmute()) {
                PacketDistributor.sendToServer(new TransmuteBookPacket(0));
            }
        }

        else if (node instanceof ReplicateNode replicateNode) {
            PacketDistributor.sendToServer(new ReplicateBookPacket(0));
        }
    }

    public long getMouseHeldTime() {
        if(nodeHeld == null) return 0;
        return System.currentTimeMillis() - holdStartTime;
    }
}

