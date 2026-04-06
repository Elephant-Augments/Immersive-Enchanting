package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.gui.canvas.CanvasRenderable;
import me.alfie.immersiveenchanting.gui.ScreenEventListener;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class Node extends CanvasRenderable implements ScreenEventListener {

    public static final int WIDTH = 26;
    public static final int HEIGHT = 26;
    public static final float DEFAULT_SCALE = 0.8f;
    public static final float HOVER_SCALE = 1f;

    private NodeState state;
    private NodeTier tier;

    private Identifier enchantmentId;
    private int enchantmentLevel;

    @Nullable
    private Identifier iconTexture;

    public Node(@NotNull Identifier enchantmentId, int enchantmentLevel, Canvas canvas, NodeState state, NodeTier tier) {
        super(canvas);
        this.enchantmentId = enchantmentId;
        this.enchantmentLevel = enchantmentLevel;
        this.state = state;
        this.tier = tier;

        setIconTexture();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        setScaleKeepPos(BranchManager.getNodeBranchScale(), state.getSpriteForTier(tier));

        if(canvas().isMouseOver(canvasX(), canvasY(),
                getScaledLength(Node.WIDTH), getScaledLength(Node.HEIGHT),
                mouseX, mouseY)) {
            setScaleKeepPos(HOVER_SCALE, state.getSpriteForTier(tier));
            canvas().screen().setNextNodeTooltip(this);
        }

        blit(graphics, state.getSpriteForTier(tier));

        if(iconTexture != null) {
            blit(graphics, iconTexture, 16, 16, 4, 4);
        }
    }

    private void setIconTexture() {
        //Use enchantmentId to find texture

        //Default to ancient book texture
        iconTexture = Identifier.fromNamespaceAndPath("immersiveenchanting", "textures/item/ancient_book.png");

        if(isState(NodeState.LOCKED)) iconTexture = null;
    }

    public NodeTier getTier() {
        return tier;
    }

    public NodeState getState() {
        return state;
    }

    public Identifier enchantmentId() {
        return enchantmentId;
    }

    public int enchantmentLevel() {
        return enchantmentLevel;
    }

    public boolean isState(NodeState state) {
        return this.state == state;
    }
}
