package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.gui.CanvasRenderable;
import me.alfie.immersiveenchanting.gui.ScreenEventListener;
import me.alfie.immersiveenchanting.gui.ScrollableCanvas;
import me.alfie.immersiveenchanting.gui.Sprite;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

public class Node extends CanvasRenderable implements ScreenEventListener {


    public static final int WIDTH = 26;
    public static final int HEIGHT = 26;

    private NodeState state;
    private NodeTier tier;

    @Nullable
    private Identifier iconTexture;

    public Node(ScrollableCanvas canvas, NodeState state, NodeTier tier) {
        super(canvas);
        this.state = state;
        this.tier = tier;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        blit(state.getSpriteForTier(tier), graphics);

        final int iconOffset = 4;
        setPos(getX()+iconOffset, getY()+iconOffset);
        if(this.iconTexture != null) blit(iconTexture, 16, 16, graphics);
        setPos(getX()-iconOffset, getY()-iconOffset);
    }

    public void setIconTexture(@Nullable Identifier identifier) {
        this.iconTexture = identifier;
    }

    public boolean isState(NodeState state) {
        return this.state == state;
    }
}
