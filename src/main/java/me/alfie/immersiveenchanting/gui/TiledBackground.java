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
                double tileX = TILE_SIZE * xo;
                double tileY = TILE_SIZE * yo;
                setPos(tileX, tileY);

                //Viewport culling
                if(getScreenX()+TILE_SIZE < canvas().getViewportRect().x1() / canvas().scale()) continue;
                if(getScreenX()-TILE_SIZE > canvas().getViewportRect().x2() / canvas().scale()) continue;
                if(getScreenY()+TILE_SIZE < canvas().getViewportRect().y1() / canvas().scale()) continue;
                if(getScreenY()-TILE_SIZE > canvas().getViewportRect().y2() / canvas().scale()) continue;

                blit(Sprite.BACKGROUND_TILE, graphics);
            }
        }
    }
}
