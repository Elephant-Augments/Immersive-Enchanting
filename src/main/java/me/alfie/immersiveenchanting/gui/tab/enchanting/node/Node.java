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
 * Represents a single node in the enchanting tree UI.
 *
 * <p>A {@code Node} is a renderable element within the {@link Canvas} that can represent
 * either a real enchantment or a special non-enchantment action (e.g. transmute, replicate).
 * Each node has a {@link NodeState} (e.g. locked, available) and a {@link NodeTier}
 * which determines its visual appearance.</p>
 *
 * <p>Nodes are rendered in canvas space and are responsible for:
 * <ul>
 *     <li>Displaying their background sprite based on state and tier</li>
 *     <li>Displaying an optional icon</li>
 *     <li>Triggering tooltip rendering when hovered</li>
 * </ul>
 * </p>
 *
 * <p>Actual interaction (e.g. clicking) is typically handled at the screen level.</p>
 */
public class Node extends CanvasRenderable {

    public static final int WIDTH = 26;
    public static final int HEIGHT = 26;
    public static final float DEFAULT_SCALE = 0.8f;
    public static final float HOVER_SCALE = 1f;

    private NodeState state;
    private NodeTier tier;

    @Nullable
    private NodeIcon icon;
    private Component title;

    private CanvasRenderable renderableIcon;

    private NodeBranch parentBranch;
    private final int position;
    private NodeData<?> data;

     /**
     * Constructs a node that represents a real enchantment with a specific level.
     *
     * <p>This constructor should be used for standard enchantment nodes that
     * correspond to a valid enchantment ID and level.</p>
     *
     * @param position The level of the enchantment (must be > 0)
     * @param canvas The canvas this node belongs to
     * @param state The current state of the node (e.g. locked, unlocked)
     * @param tier The visual tier of the node
     * @param parentBranch The branch this node belongs to
     */
    public Node(Component title,
                int position,
                Canvas canvas,
                NodeState state,
                NodeTier tier,
                @Nullable NodeIcon icon,
                NodeBranch parentBranch,
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

    public void click() {
        data.onClick(new NodeClickContext(canvas().screen(), this));
    }

    public Identifier dataType() {
        return data.type();
    }

    public NodeData<?> data() {
        return data;
    }

    public boolean isDataType(Identifier id) {
        return data.type() == id;
    }

    /**
     * Returns {@code true} if this node's enchantment can currently be removed.
     * Removal is only permitted when the node's position matches the highest level on the item
     * (i.e. it is the top-most equipped level) and removal is enabled in server config.
     */
    public boolean canRemove() {
        return getPosition() == canvas().screen()
                .getMenu()
                .getToolSlot()
                .getItem()
                .getEnchantmentLevel(EnchantmentUtil.toHolder(branchId(), canvas().screen().registryAccess()))
                && ServerConfig.isEnchantmentRemovalAllowed();
    }

    public NodeBranch getParentBranch() {
        return parentBranch;
    }

    /**
     * Renders this node on the screen.
     *
     * <p>This method first applies the node's current scale. If no other node has
     * requested a tooltip for this frame, and the mouse is hovering over this node,
     * it registers itself as the next node tooltip and skips rendering the node visuals.
     * If this node is already the pending tooltip, rendering is also skipped.</p>
     *
     * <p>Otherwise, the node's background sprite is drawn, and if an icon texture is
     * set, it is rendered on top of the background.</p>
     *
     * @param graphics The graphics context used for rendering
     * @param mouseX The current mouse X position
     * @param mouseY The current mouse Y position
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
     * Determines and assigns the icon texture for this node.
     *
     * <p>Defaults to an "ancient book" texture unless the node is in a locked state,
     * in which case no icon is rendered.</p>
     */
    private void setIcon(NodeIcon icon) {
        this.icon = icon;

        if(isState(NodeState.LOCKED) || isState(NodeState.ALERT)) this.icon = null;
    }

    /**
     * @return The visual tier of this node
     */
    public NodeTier getTier() {
        return tier;
    }

    /**
     * @return The current state of this node
     */
    public NodeState getState() {
        return state;
    }

    /**
     * @return The icon texture
     */
    public @Nullable NodeIcon getIcon() {
        return icon;
    }

    /**
     * @return The identifier associated with this node
     */
    public Identifier branchId() {
        return getParentBranch().id();
    }

    /**
     * Gets the localized display title for this node.
     *
     * <p>For enchantment nodes, this includes the enchantment name and level.
     * For non-enchantment nodes, this resolves based on the node ID.</p>
     *
     * @return A {@link Component} representing the node title
     */
    public Component getTitle() {
        return title;
    }

    /**
     * Gets the enchantment level of this node.
     *
     * @return The enchantment level
     * @throws IllegalStateException if this node does not represent an enchantment
     */
    public int getPosition() {
        return position;
    }

    /**
     * Checks if this node is in a given state.
     *
     * @param state The state to compare against
     * @return {@code true} if the node is in the specified state
     */
    public boolean isState(NodeState state) {
        return this.state == state;
    }
}
