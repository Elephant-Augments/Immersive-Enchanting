package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import com.mojang.blaze3d.platform.NativeImage;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.gui.CanvasRenderable;
import me.alfie.immersiveenchanting.gui.ScrollableCanvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dynamically creates line textures between nodes in a NodeBranch.
 */
public class BranchTexture extends CanvasRenderable {

    private final NodeBranch branch;

    private final List<Pixel> precomputedPixels = new ArrayList<>();
    private Identifier textureId;
    private int textureWidth;
    private int textureHeight;

    public BranchTexture(NodeBranch branch, ScrollableCanvas canvas) {
        super(canvas);
        this.branch = branch;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        blit(textureId, textureWidth, textureHeight, graphics);
    }

    public void calculateNodeConnections() {
        for (int i = 0; i < branch.nodes().size(); i++) {
            if (i == 0) {
                connectNodeToCenter(branch.nodes().get(i)); //Connect first node to the center
            } else {
                connectNodes(branch.nodes().get(i - 1), branch.nodes().get(i)); //Connect nodes together
            }
        }
    }

    private void connectNodeToCenter(Node node) {
        int ax = canvas().getLocalCenterPos(0, 0).x();
        int ay = canvas().getLocalCenterPos(0, 0).y();
        int bx = Math.round((float)(node.getX() + Node.WIDTH * node.getScale() / 2));
        int by = Math.round((float)(node.getY() + Node.HEIGHT * node.getScale() / 2));
        makeTexture(ax, ay, bx, by);
    }

    private void connectNodes(Node node1, Node node2) {
        int ax = (int) (node1.getX() + Node.WIDTH * node1.getScale() / 2);
        int ay = (int) (node1.getY() + Node.HEIGHT * node1.getScale() / 2);
        int bx = (int) (node2.getX() + Node.WIDTH * node2.getScale() / 2);
        int by = (int) (node2.getY() + Node.HEIGHT * node2.getScale() / 2);
        makeTexture(ax, ay, bx, by);
    }

    private void calculateBorderPixel(int x, int y, Set<Long> white, int color) {
        long k = key(x, y);
        if (!white.contains(k)) {
            precomputedPixels.add(new Pixel(x, y, color));
        }
    }

    private void makeTexture(int x1, int y1, int x2, int y2) {
        final int WHITE = 0xFFFFFFFF;
        final int BLACK = 0xFF000000;

        Set<Long> whitePixels = new HashSet<>();

        // --- PASS 1: Bresenham line ---
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        int cx = x1;
        int cy = y1;

        while (true) {
            whitePixels.add(key(cx, cy));
            if (cx == x2 && cy == y2) break;

            int e2 = err * 2;
            if (e2 > -dy) {
                err -= dy;
                cx += sx;
            }
            if (e2 < dx) {
                err += dx;
                cy += sy;
            }
        }

        // --- PASS 2: Add white pixels to precomputed list ---
        for (long p : whitePixels) {
            int px = (int) (p >> 32);
            int py = (int) (p & 0xFFFFFFFF);
            precomputedPixels.add(new Pixel(px, py, WHITE));
        }

        // --- PASS 3: Add black border pixels ---
        for (long p : whitePixels) {
            int px = (int) (p >> 32);
            int py = (int) (p & 0xFFFFFFFF);

            calculateBorderPixel(px - 1, py, whitePixels, BLACK);
            calculateBorderPixel(px + 1, py, whitePixels, BLACK);
            calculateBorderPixel(px, py - 1, whitePixels, BLACK);
            calculateBorderPixel(px, py + 1, whitePixels, BLACK);

            // Optional corners:
            calculateBorderPixel(px - 1, py - 1, whitePixels, BLACK);
            calculateBorderPixel(px + 1, py - 1, whitePixels, BLACK);
            calculateBorderPixel(px - 1, py + 1, whitePixels, BLACK);
            calculateBorderPixel(px + 1, py + 1, whitePixels, BLACK);
        }

        bakeTexture();
    }

    private void bakeTexture() {
        if (precomputedPixels.isEmpty()) return;

        // compute bounds
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

        for (Pixel p : precomputedPixels) {
            minX = Math.min(minX, p.x());
            minY = Math.min(minY, p.y());
            maxX = Math.max(maxX, p.x());
            maxY = Math.max(maxY, p.y());
        }

        textureWidth = maxX - minX + 1;
        textureHeight = maxY - minY + 1;

        setPos(minX, minY);

        // Create dynamic texture
        String label = "branch_connection";
        DynamicTexture bakedTexture = new DynamicTexture(label, textureWidth, textureHeight, true);
        textureId = Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, label + hashCode());
        Minecraft.getInstance().getTextureManager().register(textureId, bakedTexture);


        NativeImage image = bakedTexture.getPixels();

        // fill with transparent
        image.fillRect(0, 0, textureWidth, textureHeight, 0x00000000);

        // draw all pixels into texture
        for (Pixel p : precomputedPixels) {
            int px = p.x() - minX;
            int py = p.y() - minY;
            image.setPixelABGR(px, py, p.color());
        }

        bakedTexture.upload(); // upload to GPU
    }

    private long key(int x, int y) {
        return (((long) x) << 32) | (y & 0xFFFFFFFFL);
    }
}
