package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.api.node.*;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.gui.canvas.CanvasRenderable;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

/**
 * A renderable interactive node within the enchanting tree UI.
 * <p>
 * A {@code Node} represents a single element in a {@link Canvas}, defined by a
 * {@link NodeTemplate} and instantiated at runtime. Nodes may represent enchantments
 * or special actions depending on their associated {@link NodeData}.
 * <p>
 * Each node has:
 * <ul>
 *     <li>A visual {@link NodeState} (locked, available, obtained, etc.)</li>
 *     <li>A {@link NodeTier} controlling its appearance</li>
 *     <li>An optional {@link NodeIcon}</li>
 *     <li>Associated {@link NodeData} defining click behavior</li>
 * </ul>
 * <p>
 * Nodes are responsible only for rendering and interaction dispatch.
 * Game logic is handled via {@link NodeData}.
 */
public class Node extends CanvasRenderable {

    public static final int WIDTH = 26;
    public static final int HEIGHT = 26;
    public static final float DEFAULT_SCALE = 0.8f;
    public static final float HOVER_SCALE = 1f;

    private final NodeState state;
    private final NodeTier tier;

    @Nullable
    private NodeIcon icon;
    private final Component title;

    private CanvasRenderable renderableIcon;

    private final NodeBranch parentBranch;
    private final int position;
    private final NodeData<?> data;

    /**
     * Constructs a node instance.
     *
     * @param title         display name shown in the UI
     * @param position      logical position within the branch (0 if non-enchantment node)
     * @param canvas        owning canvas
     * @param state        initial node state
     * @param tier         visual tier of the node
     * @param icon         optional visual icon
     * @param parentBranch branch this node belongs to
     * @param data         interaction and behavior data for this node
     */
    public Node(Canvas canvas, NodeBranch parentBranch, int position, Component title,
                NodeState state,
                NodeTier tier,
                @Nullable NodeIcon icon,
                NodeData<?> data) {
        super(canvas);
        if(position < 0) throw new IllegalStateException("Node position can't be less than 0!");

        this.position = position;
        this.state = state;
        this.tier = tier;
        this.parentBranch = parentBranch;
        this.title = title;
        this.data = data;

        setIcon(icon);
    }

    /**
     * Triggers this node's click behavior.
     */
    public void click() {
        data.onClick(new NodeClickContext(canvas().screen(), this));
    }

    /**
     * Returns the type identifier of this node's data.
     *
     * @return node data type identifier
     */
    public Identifier dataType() {
        return data.type();
    }

    /**
     * Checks whether this node's data type matches the given identifier.
     *
     * @param id type identifier to compare
     * @return {@code true} if types match
     */
    public boolean isDataType(Identifier id) {
        return data.type() == id;
    }

    /**
     * Used internally for enchantment removal.
     * Determines whether this node can currently be removed based on its
     * enchantment level and server configuration rules.
     *
     * @return {@code true} if removal is allowed
     */
    public boolean canRemove() {
        return getPosition() == canvas().screen()
                .getMenu()
                .getToolSlot()
                .getItem()
                .getEnchantmentLevel(EnchantmentUtil.toHolder(branchId(), canvas().screen().registryAccess()))
                && ServerConfig.isEnchantmentRemovalAllowed();
    }

    /**
     * @return parent branch of this node
     */
    public NodeBranch getParentBranch() {
        return parentBranch;
    }

    /**
     * Renders this node in the UI.
     * <p>
     * Handles scaling, hover detection, tooltip registration, and icon rendering.
     */
    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        setScaleKeepPos(BranchManager.getNodeBranchScale(), state.getSpriteForTier(tier));

        if(canvas().isMouseOver(
                canvasX(), canvasY(),
                getScaledLength(Node.WIDTH), getScaledLength(Node.HEIGHT),
                mouseX, mouseY)) {

            if(!canvas().screen().camera().isDragging()) {
                int priority = canvas().screen()
                        .enchantingTab()
                        .branchManager()
                        .getAllNodes()
                        .indexOf(this);

                canvas().screen().tooltipManager().requestTooltip(this, priority);
            }

        }

        if(canvas().screen().tooltipManager().isActiveTooltipFor(this)) return;
        blit(graphics, state.getSpriteForTier(tier), canvas().getCurrentBrightness());

        if(getIcon() instanceof SpriteIcon sprite) {
            blit(graphics, sprite.id(), 16, 16, 4, 4, canvas().getCurrentBrightness());
        } else if(getIcon() instanceof ItemIcon item) {
            item(graphics, item.stack(), 4, 4, canvas().getCurrentBrightness());
        }
    }

    /**
     * Assigns and validates the node icon.
     * Locked or alert-state nodes will not display icons.
     */
    private void setIcon(NodeIcon icon) {
        this.icon = icon;

        if(isState(NodeState.LOCKED) || isState(NodeState.ALERT)) this.icon = null;
    }

    /** @return visual tier of this node */
    public NodeTier getTier() {
        return tier;
    }

    /** @return current state of this node */
    public NodeState getState() {
        return state;
    }

    /** @return optional node icon */
    public @Nullable NodeIcon getIcon() {
        return icon;
    }

    /** @return identifier of the parent branch */
    public Identifier branchId() {
        return getParentBranch().id();
    }

    /** @return display title of this node */
    public Component getTitle() {
        return title;
    }

    /**
     * Returns the node position within its branch.
     */
    public int getPosition() {
        return position;
    }

    /**
     * Checks whether this node is in the specified state.
     *
     * @param state state to compare
     * @return {@code true} if matching
     */
    public boolean isState(NodeState state) {
        return this.state == state;
    }
}
