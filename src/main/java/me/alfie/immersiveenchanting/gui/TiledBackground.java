package me.alfie.immersiveenchanting.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public class TiledBackground extends CanvasRenderable {

    protected static final int TILE_SIZE = 16;

    public TiledBackground(ScrollableCanvas canvas) {
        super(canvas);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        for (int xo = 0; xo < canvas().tiles(); xo++) {
            for (int yo = 0; yo < canvas().tiles(); yo++) {
                setPos(TILE_SIZE * xo, TILE_SIZE * yo);
                blit(Sprite.BACKGROUND_TILE, graphics);
            }
        }
    }
}
