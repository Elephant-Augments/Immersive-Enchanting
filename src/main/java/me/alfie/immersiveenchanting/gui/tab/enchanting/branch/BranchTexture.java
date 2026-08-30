package me.alfie.immersiveenchanting.gui.tab.enchanting.branch;

import com.mojang.blaze3d.platform.NativeImage;
import me.alfie.alfinolib.gui.GuiGraphicsX;
import me.alfie.alfinolib.gui.util.MousePos;
import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.canvas.CanvasRenderable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.joml.Vector2i;

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
    private ResourceId textureId;
    private int textureWidth;
    private int textureHeight;

    public BranchTexture(NodeBranch branch, Canvas canvas) {
        super(canvas);
        this.branch = branch;
    }

    @Override
    public void render(GuiGraphicsX gx, MousePos mousePos) {
        int brightness = shouldBrightenBranch()
                ? Canvas.FULL_BRIGHTNESS
                : canvas().getCurrentBrightness();

        blit(gx, textureId, textureWidth, textureHeight, brightness);
    }

    private boolean shouldBrightenBranch() {
        Node hovered = canvas().screen().tooltipManager().getActiveTooltipNode();
        if(hovered == null) {
            return false;
        }
        if(branch.nodes().contains(hovered)) {
            return true;
        }
        for(Node node : branch.nodes()) {
            if(node.isMutexPartnerOfHoveredNode()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Draws connector lines between every consecutive node in the branch and from the
     * first node back to the canvas center (or to an attached origin node), then bakes
     * the result into a GPU texture.
     * Must be called after nodes have been positioned via {@link NodeBranch#placeNodesAlongLine(BranchSpacing)}.
     */
    public void calculateNodeConnections() {
        if (branch.nodes().isEmpty()) return;

        precomputedPixels.clear();

        Node origin = branch.originNode();
        if(origin != null) {
            connectNodes(origin, branch.nodes().getFirst());
        } else {
            connectNodeToCenter(branch.nodes().getFirst());
        }

        for (int i = 1; i < branch.nodes().size(); i++) {
            connectNodes(branch.nodes().get(i - 1), branch.nodes().get(i));
        }
        bakeTexture();
    }

    private void connectNodeToCenter(Node node) {
        Vector2i center = canvas().getCenter();
        float[] edge = new float[2];
        float cx = NodeBranch.centerX(node);
        float cy = NodeBranch.centerY(node);
        float angle = NodeBranch.angleToward(center.x(), center.y(), cx, cy);
        NodeBranch.borderPoint(node, angle + (float) Math.PI, edge);

        makeTexture(center.x(), center.y(), Math.round(edge[0]), Math.round(edge[1]));
    }

    private void connectNodes(Node node1, Node node2) {
        float ax = NodeBranch.centerX(node1);
        float ay = NodeBranch.centerY(node1);
        float bx = NodeBranch.centerX(node2);
        float by = NodeBranch.centerY(node2);

        float angle = NodeBranch.angleToward(ax, ay, bx, by);
        float[] start = new float[2];
        float[] end = new float[2];
        NodeBranch.borderPoint(node1, angle, start);
        NodeBranch.borderPoint(node2, angle + (float) Math.PI, end);

        makeTexture(Math.round(start[0]), Math.round(start[1]), Math.round(end[0]), Math.round(end[1]));
    }

    private void calculateBorderPixel(int x, int y, Set<Long> white, int color) {
        long k = key(x, y);
        if (!white.contains(k)) {
            precomputedPixels.add(new Pixel(x, y, color));
        }
    }

    /**
     * Rasterizes a 1-pixel-wide line between two canvas points using Bresenham's algorithm,
     * then adds a 1-pixel black border around every white pixel. The resulting pixels are
     * appended to {@link #precomputedPixels} for later baking.
     */
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
        setCanvasPos(minX, minY);

        // Create dynamic texture
        String label = "branch_connection";
        DynamicTexture bakedTexture = new DynamicTexture(textureWidth, textureHeight, true);
        textureId = new ResourceId(ImmersiveEnchanting.MODID, label + hashCode());
        Minecraft.getInstance().getTextureManager().register(textureId.mc(), bakedTexture);


        NativeImage image = bakedTexture.getPixels();

        // fill with transparent
        image.fillRect(0, 0, textureWidth, textureHeight, 0x00000000);

        // draw all pixels into texture
        for (Pixel p : precomputedPixels) {
            int px = p.x() - minX;
            int py = p.y() - minY;
            image.setPixelRGBA(px, py, p.color());
        }

        bakedTexture.upload(); // upload to GPU
    }

    /** Packs two ints into a single long for use as a hash-set key. */
    private long key(int x, int y) {
        return (((long) x) << 32) | (y & 0xFFFFFFFFL);
    }
}
