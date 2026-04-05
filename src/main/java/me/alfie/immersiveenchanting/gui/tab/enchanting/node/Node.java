package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.gui.CanvasRenderable;
import me.alfie.immersiveenchanting.gui.ScreenEventListener;
import me.alfie.immersiveenchanting.gui.ScrollableCanvas;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class Node extends CanvasRenderable implements ScreenEventListener {

    public static final int WIDTH = 26;
    public static final int HEIGHT = 26;

    private NodeState state;
    private NodeTier tier;

    private Identifier enchantmentId;

    @Nullable
    private Identifier iconTexture;

    public Node(@NotNull Identifier enchantmentId, ScrollableCanvas canvas, NodeState state, NodeTier tier) {
        super(canvas);
        this.enchantmentId = enchantmentId;
        this.state = state;
        this.tier = tier;

        setIconTexture();

        setScale(1f);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.pose().pushMatrix();
        graphics.pose().scale(getScale());
        blit(state.getSpriteForTier(tier), graphics);

        final int iconOffset = (int) (4 * getScale());
        setPos(getX()+iconOffset, getY()+iconOffset);
        if(this.iconTexture != null) blit(iconTexture, 16, 16, graphics);
        setPos(getX()-iconOffset, getY()-iconOffset);
        graphics.pose().popMatrix();
    }

    private void setIconTexture() {
        //Use enchantmentId to find texture

        //Default to ancient book texture
        iconTexture = Identifier.fromNamespaceAndPath("immersiveenchanting", "textures/item/ancient_book.png");

        if(isState(NodeState.LOCKED)) iconTexture = null;
    }

    public boolean isState(NodeState state) {
        return this.state == state;
    }
}
