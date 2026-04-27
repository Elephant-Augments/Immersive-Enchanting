package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.canvas.CanvasRenderable;
import me.alfie.immersiveenchanting.util.EnchantmentTextureHelper;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

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

    private final ResourceLocation id;
    private final int enchantmentLevel;

    @Nullable
    private ResourceLocation iconTexture;

    /**
     * Constructs a node that represents a real enchantment with a specific level.
     *
     * <p>This constructor should be used for standard enchantment nodes that
     * correspond to a valid enchantment ID and level.</p>
     *
     * @param enchantmentId The identifier of the enchantment
     * @param enchantmentLevel The level of the enchantment (must be > 0)
     * @param canvas The canvas this node belongs to
     * @param state The current state of the node (e.g. locked, unlocked)
     * @param tier The visual tier of the node
     */
    public Node(@NotNull ResourceLocation enchantmentId,
                int enchantmentLevel,
                Canvas canvas,
                NodeState state,
                NodeTier tier) {
        super(canvas);
        this.id = enchantmentId;
        this.enchantmentLevel = enchantmentLevel;
        this.state = state;
        this.tier = tier;

        setIconTexture();
    }

    /**
     * Constructs a node that does not represent a traditional enchantment.
     *
     * <p>This is used for special-purpose nodes such as actions (e.g. transmute,
     * replicate) that do not have an enchantment level.</p>
     *
     * <p>Calling {@link #getEnchantmentLevel()} on instances created with this
     * constructor will throw an {@link IllegalStateException}.</p>
     *
     * @param id The identifier of the special node
     * @param canvas The canvas this node belongs to
     * @param state The current state of the node
     * @param tier The visual tier of the node
     */
    public Node(@NotNull ResourceLocation id,
                Canvas canvas,
                NodeState state,
                NodeTier tier) {
        super(canvas);
        this.id = id;
        this.enchantmentLevel = 1;
        this.state = state;
        this.tier = tier;

        setIconTexture();
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        setScaleKeepPos(BranchManager.getNodeBranchScale(), state.getSpriteForTier(tier));

        if(canvas().isMouseOver(
                canvasX(), canvasY(),
                getScaledLength(Node.WIDTH), getScaledLength(Node.HEIGHT),
                mouseX, mouseY)) {

            if(!canvas().screen().camera().isDragging()) canvas().screen().tooltipManager().requestTooltip(this);
        }

        if(canvas().screen().tooltipManager().isActiveTooltipFor(this)) return;
        blit(graphics, state.getSpriteForTier(tier), canvas().getCurrentBrightness());
        if(iconTexture != null) {
            blit(graphics, iconTexture, 16, 16, 4, 4, canvas().getCurrentBrightness());
        }
    }

    /**
     * Determines and assigns the icon texture for this node.
     *
     * <p>Defaults to an "ancient book" texture unless the node is in a locked state,
     * in which case no icon is rendered.</p>
     */
    private void setIconTexture() {
        iconTexture = EnchantmentTextureHelper.getTexture(id());

        if(isState(NodeState.LOCKED) || isState(NodeState.ALERT)) iconTexture = null;
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
    public @Nullable ResourceLocation getIconTexture() {
        return iconTexture;
    }

    /**
     * @return The identifier associated with this node
     */
    public ResourceLocation id() {
        return id;
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
        if(isEnchantment()) {
            return EnchantmentUtil.toHolder(id, canvas().screen().registryAccess()).get().getFullname(enchantmentLevel);
        } else {
            return Component.translatable("immersiveenchanting.tooltip.title." + id().getPath());
        }
    }

    /**
     * Checks whether this node represents a real enchantment.
     *
     * @return {@code true} if this is an enchantment node, {@code false} if it is a special node
     */
    public boolean isEnchantment() {
        return id != CostRegistry.TRANSMUTE && id != CostRegistry.REPLICATE;
    }

    /**
     * Gets the enchantment level of this node.
     *
     * @return The enchantment level
     * @throws IllegalStateException if this node does not represent an enchantment
     */
    public int getEnchantmentLevel() {
        if(enchantmentLevel == 0) throw new IllegalStateException("This node does not have an enchantment level!");
        return enchantmentLevel;
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
