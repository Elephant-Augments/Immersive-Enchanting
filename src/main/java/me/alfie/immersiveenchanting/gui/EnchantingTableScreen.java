package me.alfie.immersiveenchanting.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.datapack.LevelCost;
import me.alfie.immersiveenchanting.networking.ModPacketHandler;
import me.alfie.immersiveenchanting.networking.packets.EnchantItemPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EnchantingTableScreen extends AbstractContainerScreen<EnchantingTableMenu> {

    public static final ResourceLocation ENCHANTING_TABLE_BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID,
            "textures/gui/container/enchanting_table.png");
    public static final ResourceLocation TILE_TEXTURE = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID,
            "textures/gui/container/background.png");
    public static final ResourceLocation BOOK_OPEN_TEXTURE = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID,
            "textures/gui/container/book_open_shadow.png");
    public static final ResourceLocation ENCHANTING_TABLE_TOP_TEXTURE = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID,
            "textures/gui/container/enchanting_table_top.png");
    public static final ResourceLocation BOOK_CLOSED_TEXTURE = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID,
            "textures/gui/container/book_closed.png");

    private final int tileSize = 16; //Size of the texture (assume 16x16 for blocks)
    private final int viewportStartX = 5; //Position that viewport starts on the texture (top left)
    final int viewportStartY = 5; //Position that viewport starts on the texture (top left)
    final int viewportWidth = 247; //Dimensions of viewport in the texture
    final int viewportHeight = 117; //Dimensions of viewport in the texture
    //Virtual canvas
    int scrollableCanvasWidth = tileSize*64; //Must be divisible by tileSize (16), otherwise rendered tiles/edge constraints will leave gaps
    int scrollableCanvasHeight = tileSize*64; //Must be divisible by tileSize (16), otherwise rendered tiles/edge constraints will leave gaps

    int canvasLeftPos;
    int canvasTopPos;

    private boolean croppingEnabled = true; //Whether to crop the canvas outside of viewport - false for debugging.
    //int canvasCenterOffsetX = (imageWidth - scrollableCanvasWidth) / 2;
    int canvasCenterOffsetX = (viewportWidth - viewportStartX) / 2 - scrollableCanvasWidth/2 + tileSize;
    int canvasCenterOffsetY = (viewportHeight - viewportStartY) / 2 - scrollableCanvasHeight/2 + tileSize;

    //Fields for scrollable canvas
    double scrollX = 0;
    double scrollY = 0;
    private boolean dragging = false;
    private double dragStartMouseX = 0;
    private double dragStartMouseY = 0;
    private double dragStartScrollX = 0;
    private double dragStartScrollY = 0;
    private boolean hoveringAnyNode;
    private float currentBackgroundBrightness = 1f;
    private EnchantingNode currentNodeHover;
    private EnchantingNodeTooltip enchantingNodeTooltip;

    //Nodes
    final List<EnchantingNode> rendered_nodes = new ArrayList<>();
    public final List<EnchantingNodeBranch> branches = new ArrayList<>();

    private ItemStack lastStack = ItemStack.EMPTY; //Handling which tool in slot
    private Player player;
    private ItemStack renderCostStack; //The stack to render when hovering.
    private boolean isSlot0Empty = true;

    public final float PARALLAX = 0.5f;

    public EnchantingTableScreen(EnchantingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.titleLabelX = 10;
        this.inventoryLabelX = 10;
        this.imageHeight = 222;
        this.imageWidth = 256;

        // Center the canvas inside the viewport at start
        this.scrollX = (scrollableCanvasWidth / 2.0) - (viewportWidth / 2.0);
        this.scrollY = (scrollableCanvasHeight / 2.0) - (viewportHeight / 2.0);

        //No branch shenanigans here, do it in init() pls <3
        this.player = playerInventory.player;
    }

    @Override //Init code when GUI is created.
    public void init() {
        super.init();
        initializeScreen();
        onToolSlotChanged(); //Updates if screen size is changed while screen open
    }

    private void initializeScreen() {
        calculateViewportSize();

        //Prevent tearing
        branches.clear();
        rendered_nodes.clear();
        //onSlot0Changed();
    }

    private void calculateViewportSize() {
        //Setting dynamically requires recentering each time
        //This method creates the smallest possible screen size for the highest level of enchantment (in Vanilla, the highest level of enchantment is 5)

        //Each NODE_STEP = 80 pixels, as canvas is centred 80 pixels * 2 reaches the centre of the first node
        int pixels = (EnchantingNodeBranch.node_step*2)*EnchantmentCostRegistry.getClientRegistry().getHighestEnchantmentLevel();
        int margin = tileSize*4; //Add 4 tile margin
        int rounded = ((pixels + tileSize - 1) / tileSize) * tileSize;

        scrollableCanvasWidth  = rounded + margin;
        scrollableCanvasHeight = rounded + margin;

        scrollX = (scrollableCanvasWidth / 2.0) - (viewportWidth / 2.0);
        scrollY = (scrollableCanvasHeight / 2.0) - (viewportHeight / 2.0);

        //These have to be updated here, idk why but it just breaks ok?
        canvasLeftPos = this.leftPos + viewportStartX;
        canvasTopPos = this.topPos + viewportStartY;
    }


    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        //For culling
        int viewportLeft   = this.leftPos + viewportStartX;
        int viewportTop    = this.topPos + viewportStartY;
        int viewportRight  = viewportLeft + viewportWidth;
        int viewportBottom = viewportTop + viewportHeight;

        if(croppingEnabled) {
            double scale = this.minecraft.getWindow().getGuiScale();
            RenderSystem.enableScissor(
                    (int) (viewportLeft * scale),
                    (int) ((this.height - viewportBottom) * scale), // Y is flipped!
                    (int) (viewportWidth * scale),
                    (int) (viewportHeight * scale)
            );
        }

        //Fade the background and nodes darker if hovering
        float targetBrightness = hoveringAnyNode ? 0.15f : 1f;
        float speed = 0.3f;
        currentBackgroundBrightness += (targetBrightness - currentBackgroundBrightness) * speed;
        guiGraphics.setColor(currentBackgroundBrightness, currentBackgroundBrightness, currentBackgroundBrightness, 1f);

        //Darken background if slot 0 empty
        if(isSlot0Empty) {
            guiGraphics.setColor(0.5F, 0.5F, 0.5F, 1f);
        }

        //Draw a tiled background using TILE_TEXTURE as a background
        int numberOfTilesX = (scrollableCanvasWidth / tileSize);
        int numberOfTilesY = (scrollableCanvasHeight / tileSize);

        for (int x = 0; x < numberOfTilesX; x++) {
            for (int y = 0; y < numberOfTilesY; y++) {
                // Calculate tile screen position
                int tileScreenX = canvasLeftPos + x * tileSize - (int) scrollX;
                int tileScreenY = canvasTopPos + y * tileSize - (int) scrollY;

                // Skip tiles completely outside the viewport
                if (tileScreenX + tileSize < viewportLeft || tileScreenX > viewportRight ||
                        tileScreenY + tileSize < viewportTop || tileScreenY > viewportBottom) {
                    continue;
                }

                guiGraphics.blit(
                        TILE_TEXTURE,
                        canvasLeftPos + x * tileSize - (int)scrollX,
                        canvasTopPos + y * tileSize - (int)scrollY,
                        0f, 0f,
                        tileSize, tileSize,
                        tileSize, tileSize
                );
            }
        }
        //Reset color after darkening
        guiGraphics.setColor(1f, 1f, 1f, 1f);

        //Render connections behind nodes.
        for (EnchantingNodeBranch branch : branches) {
            guiGraphics.setColor(currentBackgroundBrightness, currentBackgroundBrightness, currentBackgroundBrightness, 1f);

            //Don't darken the hovered node branch.
            if(branch.getNodes().contains(currentNodeHover)) {
                guiGraphics.setColor(1f, 1f, 1f, 1f);
            }

            //branch.drawNodeConnections(guiGraphics);
            if(!branch.hasCalculatedConnections()) {
                branch.calculateNodeConnections();
            }

            branch.renderPrecomputedConnection(guiGraphics, (int) scrollX, (int) scrollY);

            //Reset color after darkening
            guiGraphics.setColor(1f, 1f, 1f, 1f);
        }

        //Add background book
        guiGraphics.blit(
                ENCHANTING_TABLE_TOP_TEXTURE,
                centerOnCanvas(32, 32).x  - (int)scrollX,
                centerOnCanvas(32, 32).y - (int)scrollY,
                0f, 0f, 32, 32,
                32, 32
        );

        if(this.menu.getSlot(0).getItem().isEmpty()) {
            guiGraphics.blit(
                    BOOK_CLOSED_TEXTURE,
                    centerOnCanvas(32, 32).x  - (int)scrollX,
                    centerOnCanvas(32, 32).y - (int)scrollY,
                    0f, 0f, 32, 32,
                    32, 32
            );
        } else {
            guiGraphics.blit(
                    BOOK_OPEN_TEXTURE,
                    centerOnCanvas(32, 32).x  - (int)scrollX,
                    centerOnCanvas(32, 32).y - (int)scrollY,
                    0f, 0f, 32, 32,
                    32, 32
            );
        }


        //Render the item in slot0 (tool slot)
        ItemStack stack = this.menu.getSlot(0).getItem(); //to get the ItemStack
        guiGraphics.renderItem(stack, centerOnCanvas(16, 16).x - (int)scrollX,
                centerOnCanvas(16, 16).y - (int)scrollY);

        //Render the unhovered nodes.
        for(EnchantingNode node : rendered_nodes) {
            guiGraphics.setColor(currentBackgroundBrightness, currentBackgroundBrightness, currentBackgroundBrightness, 1f);
            node.render(guiGraphics, this);

            //Reset color after darkening
            guiGraphics.setColor(1f, 1f, 1f, 1f);
        }

        hoveringAnyNode = false;
        EnchantingNode hoveredNode = null;
        // Draw node tooltips
        for (EnchantingNode node : this.rendered_nodes) {
            if (node.isMouseOver(mouseX, mouseY, this)) {
                hoveringAnyNode = true;
                hoveredNode = node;

                if (currentNodeHover != node) { //If the node hover has changed
                    currentNodeHover = node; //Set this node to current

                    // FORCE rebuild on new node
                    enchantingNodeTooltip = null;
                    renderCostStack = null;

                    player.playSound(SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED);
                }

                if (node.isBranchUnlocked) {
                    MutableComponent enchantmentName = (MutableComponent) node.getEnchantmentHolder().get().getFullname(node.getEnchantmentLevel());

                    List<Component> tooltip = List.of(enchantmentName.withStyle(ChatFormatting.WHITE));

                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0, 0, 400);

                    if (renderCostStack == null) {
                        renderCostStack = EnchantmentCostRegistry.getClientRegistry()
                                .getEnchantmentCost(node.getEnchantmentHolder().unwrapKey().get())
                                .getLevel(node.getEnchantmentLevel())
                                .asItemStack();
                    }

                    if (enchantingNodeTooltip == null) {
                        enchantingNodeTooltip = new EnchantingNodeTooltip(
                                this.font,
                                node,
                                tooltip.get(0).getString(),
                                this.renderCostStack,
                                this
                        );
                    }

                    enchantingNodeTooltip.renderEnchantmentTooltip(guiGraphics);
                    guiGraphics.pose().popPose();
                } else {
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0, 0, 400);
                    enchantingNodeTooltip = new EnchantingNodeTooltip(
                            this.font,
                            node,
                            Component.translatable("gui.immersiveenchanting.locked_enchantment").withStyle(ChatFormatting.RED).getString(),
                            this.renderCostStack,
                            this
                    );
                    enchantingNodeTooltip.renderEnchantmentTooltip(guiGraphics);
                    guiGraphics.pose().popPose();

                }
                break;

            }
        }


        if(!hoveringAnyNode && renderCostStack != null) {
            renderCostStack = null;
        }

        if(hoveredNode != currentNodeHover) {
            currentNodeHover = null;
        }

        //Render the hovered node.
        for(EnchantingNode node : rendered_nodes) {
            if(node == currentNodeHover) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 500);
                node.setScale(1f);

                node.render(guiGraphics, this);
                guiGraphics.pose().popPose();
            } else {
                node.setScale(EnchantingNode.globalScale);
            }
        }


        // Disable scissor after drawing
        RenderSystem.disableScissor();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(ENCHANTING_TABLE_BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        RenderSystem.disableBlend();
    }

    public ArrayList<Float> generateBranchAngles(int totalBranches) {
        // No more than 16 branches
        ArrayList<Float> angles = new ArrayList<>();
        for (int i = 0; i < totalBranches; i++) {
            float angle = (float) (i * 2 * Math.PI / totalBranches); // evenly spaced
            angles.add(angle);
        }
        return angles;
    }

    public void buildNodeBranches(ItemStack currentItemStack) {
        //Step 1. Find which enchantments are applicable.
        Set<Holder<Enchantment>> validEnchantments = new HashSet<>(); //Set of all enchantments that can be applied
        Set<Holder<Enchantment>> unlockedEnchantments = menu.getUnlockedEnchantments();

        //RegistryAccess registryAccess = player.registryAccess();
        //HolderLookup.RegistryLookup<Enchantment> lookup = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        //List<Holder.Reference<Enchantment>> allEnchantments = lookup.listElements().toList();

        Set<Holder<Enchantment>> allEnchantments = ImmersiveEnchanting.getEnchantmentRegistry(
                player.level().registryAccess())
                .asLookup()
                .listElements()
                .collect(Collectors.toSet());

        //Iterate through the enchantment registry, see if the item support the enchantment.
        for (Holder<Enchantment> enchantmentHolder : allEnchantments) {
            //Skip cursed enchantments.
            if (enchantmentHolder.get().isCurse()) {
                continue;
            }

            ResourceKey<Enchantment> enchantmentKey = enchantmentHolder.unwrapKey().get();

            //If enchantment has DO_NOT_INCLUDE tag. (Empty json)
            if(EnchantmentCostRegistry.getClientRegistry().getCostRegistry().containsKey(enchantmentKey)) {
                if(EnchantmentCostRegistry.getClientRegistry().getCostRegistry()
                        .get(enchantmentKey)
                        .getLevel(-1).item().equals(LevelCost.DO_NOT_INCLUDE)) {
                    continue;
                }
            }



            //If this enchantment isn't compatible with any enchantments already applied to the item, then skip.
            Set<Enchantment> itemEnchantments = new HashSet<>(currentItemStack.getAllEnchantments().keySet());
            itemEnchantments.remove(enchantmentHolder.get()); //Ignore the enchantment we're trying to check for

            if(!EnchantmentHelper.isEnchantmentCompatible(itemEnchantments, enchantmentHolder.get())) {
                continue;
            }

            //Add enchantment
            if (enchantmentHolder.get().canEnchant(currentItemStack)) {
                validEnchantments.add(enchantmentHolder);
            }
        }

        // Step 2: Generate angles after filtering
        List<Float> angles = generateBranchAngles(validEnchantments.size());

        int i = 0;
        for (Holder<Enchantment> enchantmentHolder : validEnchantments) {
            //Check if the enchantmentResourceId is unlocked.
            boolean isUnlocked = unlockedEnchantments.contains(enchantmentHolder);
            AtomicInteger enchantmentLevel = new AtomicInteger();

            //Get the level of this enchantment
            enchantmentLevel.set(currentItemStack.getItem().getEnchantmentLevel(currentItemStack, enchantmentHolder.get()));

            branches.add(new EnchantingNodeBranch(
                    this,
                    angles.get(i),
                    enchantmentHolder,
                    enchantmentLevel.get(),
                    isUnlocked,
                    player
            ));
            i++;
        }
    }


    public void onToolSlotChanged() {
        player.playSound(SoundEvents.BOOK_PAGE_TURN);

        //Clear all nodes
        branches.clear();
        rendered_nodes.clear();

        ItemStack current_item = this.menu.getSlot(0).getItem();
        if(current_item.isEmpty()) {
            initializeScreen();
            isSlot0Empty = true;
        } else {
            //calculateViewportSize();
            isSlot0Empty = false;
        }

        buildNodeBranches(current_item);

        //Update canvas size
        int largestBranchLevel = 1;
        for (EnchantingNodeBranch branch : branches) {
            largestBranchLevel = Math.max(largestBranchLevel, branch.getNodes().size());
        }

        EnchantingNodeBranch.calculateNodeAnglesAndStep(this);
        //Place nodes after size change
        for (EnchantingNodeBranch branch : branches) {
            branch.placeNodesAlongLine();
            for(EnchantingNode node : branch.getNodes()) {
                node.setScale(EnchantingNode.globalScale);
            }
        }


    }

    @Override
    public void containerTick() {
        super.containerTick();

        ItemStack current_item = this.menu.getSlot(0).getItem();
        if (!ItemStack.isSameItemSameTags(current_item, lastStack)) {
            this.lastStack = current_item.copy();
            onToolSlotChanged();
        }

    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        //Inventory labels not required for this GUI.
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        //Draw item tooltips
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    //Scrollable functionality
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !isSlot0Empty) {
            // 1. Check if any node was clicked
            for (EnchantingNode node : this.rendered_nodes) {
                if (node.isMouseOver(mouseX, mouseY, this)) {
                    this.onNodeClicked(node);
                    return true; // consume the click, don't drag
                }
            }

            // 2. Start a drag if no node clicked
            // Compute viewport bounds in screen coordinates
            int viewportLeft   = this.leftPos + viewportStartX;
            int viewportTop    = this.topPos  + viewportStartY;
            int viewportRight  = viewportLeft + viewportWidth;
            int viewportBottom = viewportTop + viewportHeight;

            // Only start dragging if the mouse is inside the viewport
            if (mouseX >= viewportLeft && mouseX < viewportRight &&
                    mouseY >= viewportTop  && mouseY < viewportBottom) {
                dragging = true;
                dragStartMouseX = mouseX;
                dragStartMouseY = mouseY;
                dragStartScrollX = scrollX;
                dragStartScrollY = scrollY;
                return true; // consume the click
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            scrollX = dragStartScrollX + (dragStartMouseX - mouseX);
            scrollY = dragStartScrollY + (dragStartMouseY - mouseY);

            //Left Edge
            if (scrollX+canvasLeftPos < canvasLeftPos) {
                scrollX = 0;
            }

            //Top Edge
            if (scrollY+canvasTopPos < canvasTopPos) {
                scrollY = 0;
            }

            // Right edge
            if (scrollX > scrollableCanvasWidth - viewportWidth) {
                scrollX = scrollableCanvasWidth - viewportWidth;
            }

            // Bottom edge
            if (scrollY > scrollableCanvasHeight - viewportHeight) {
                scrollY = scrollableCanvasHeight - viewportHeight;
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);

    }


    //Helper
    public Vector2i centerOnCanvas(int textureWidth, int textureHeight) {
        int x = canvasLeftPos + scrollableCanvasWidth/2 - textureWidth/2;
        int y = canvasTopPos + scrollableCanvasHeight/2 - textureHeight/2;
        return new Vector2i(x, y);
    }

    private void onNodeClicked(EnchantingNode node) {
        if (!node.isObtained() && node.isBranchUnlocked) {
            //Get item at slot
            //player.level().playSound(player, player.blockPosition(),
            //SoundEvents.BEACON_POWER_SELECT, SoundSource.MASTER,
            //        1.0F, 1.0F);

            ItemStack stack = this.menu.getSlot(0).getItem();

            //Give server ResourceKey<Enchantment>.location().toString()
            //Server uses RESOURCE_KEY_MAP.get() to find the corresponding ResourceKey
            //Server enchants tool, client side cannot do it.
            ModPacketHandler.INSTANCE.sendToServer(new EnchantItemPacket(
                    node.getEnchantmentHolder().unwrapKey().get(),
                    node.getEnchantmentLevel()
            ));
        }

    }

    public void setRenderCostTo(ItemStack costStack) {
        this.renderCostStack = costStack;
    }
}
