package me.alfie.immersiveenchanting.gui;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2i;

import java.util.*;

/**
 * Generic class that
 */
public class NodeBranch {

    public static int node_step = 80;
    private final List<Node> nodes = new ArrayList<>(); //All nodes in this branch, order determines render order.
    private final float branchAngle; //Angle of this branch

    private final EnchantingTableScreen screen;

    //Texture baking
    private final List<Pixel> precomputedPixels = new ArrayList<>();
    ResourceLocation bakedTextureLocation;
    int texOriginX;
    int texOriginY;
    int texWidth;
    int texHeight;
    private boolean isTextureReady;

    public NodeBranch(EnchantingTableScreen screen, float branchAngle) {
        this.screen = screen;
        this.branchAngle = branchAngle;
    }

    public void addNode(Node node) {
        nodes.add(node);
        screen.rendered_nodes.add(node);
    }

    public static void calculateNodeAnglesAndStep(EnchantingTableScreen screen) {
        if (screen == null || screen.branches.isEmpty() || screen.branches.size() == 1) {
            node_step = 40;
            EnchantingNode.globalScale = 1.0f;
            return;
        }

        // Config
        final int baseStep = 40;      // ideal node distance
        final int minStep = 40;       // minimum allowed
        final int maxStep = 120;      // maximum allowed
        final float minScale = 0.25f; // never go below this
        final float maxScale = 1.0f;  // never go above this
        final float nodeSize = Math.max(Node.width, Node.height);

        List<NodeBranch> branches = screen.branches;
        int count = branches.size();

        // Sort branches by angle
        branches.sort(Comparator.comparingDouble(b -> b.branchAngle));

        // --- Step 1: compute smallest angular distance ---
        double smallestAngle = Double.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            double a1 = branches.get(i).branchAngle;
            double a2 = branches.get((i + 1) % count).branchAngle;
            double diff = Math.abs(a2 - a1);
            diff = Math.min(diff, 2 * Math.PI - diff);
            smallestAngle = Math.min(smallestAngle, diff);
        }

        // --- Step 2: try to keep node_step fixed ---
        node_step = baseStep;

        // Check if angular spacing is enough to avoid overlap at node_step
        double minRequiredDistance = nodeSize; // minimal distance to avoid overlap
        double currentDistance = node_step * smallestAngle;

        if (currentDistance < minRequiredDistance) {
            // Need to increase node_step proportionally
            node_step = (int) Math.ceil(minRequiredDistance / smallestAngle);
        }

        // Clamp node_step
        node_step = Math.max(minStep, Math.min(node_step, maxStep));

        // --- Step 3: scale down only if node_step hit max ---
        float scale = 1.0f;
        currentDistance = node_step * smallestAngle;
        if (currentDistance < minRequiredDistance) {
            scale = (float) (currentDistance / minRequiredDistance);
            scale = Math.max(scale, minScale);
        }
        scale = Math.min(scale, maxScale);

        EnchantingNode.globalScale = scale;
    }

    /**
     * Generate a list of angles based on the total number of branches.
     *
     * @param totalBranches
     * @return
     */
    public static ArrayList<Float> generateBranchAngles(int totalBranches) {
        // No more than 16 branches
        ArrayList<Float> angles = new ArrayList<>();
        for (int i = 0; i < totalBranches; i++) {
            float angle = (float) (i * 2 * Math.PI / totalBranches); // evenly spaced
            angles.add(angle);
        }
        return angles;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void placeNodesAlongLine() {
        // Center point of the canvas
        Vector2i center = new Vector2i(
                screen.canvasLeftPos + screen.scrollableCanvasWidth / 2,
                screen.canvasTopPos + screen.scrollableCanvasHeight / 2
        );

        // Step vector based on angle
        double stepX = Math.cos(branchAngle) * node_step; // 48px per node
        double stepY = Math.sin(branchAngle) * node_step;

        for (int i = 0; i < nodes.size(); i++) {
            int x = (int) Math.round(center.x + stepX * (i + 1)); // start at 48px
            int y = (int) Math.round(center.y + stepY * (i + 1));

            nodes.get(i).setX(x - Node.width / 2);
            nodes.get(i).setY(y - Node.height / 2);
        }
    }

    public void calculateNodeConnections() {
        //Loop through all nodes in this branch
        for (int i = 0; i < nodes.size(); i++) {
            if (i == 0) {
                connectNodeToCenter(nodes.get(i)); //Connect first node to the center
            } else {
                connectNodes(nodes.get(i - 1), nodes.get(i)); //Connect nodes together automatically
            }
        }
    }

    private void connectNodeToCenter(Node node) {
        // Use node centers for cleaner lines
        int ax = screen.canvasLeftPos + screen.scrollableCanvasWidth / 2;
        int ay = screen.canvasTopPos + screen.scrollableCanvasHeight / 2;
        int bx = (int) (node.getX() + Node.width * node.getScale() / 2);
        int by = (int) (node.getY() + Node.height * node.getScale() / 2);
        calculateConnection(ax, ay, bx, by);
    }

    //Connect two nodes together
    private void connectNodes(Node node1, Node node2) {
        // Use node centers for cleaner lines
        int ax = (int) (node1.getX() + Node.width * node1.getScale() / 2);
        int ay = (int) (node1.getY() + Node.height * node1.getScale() / 2);
        int bx = (int) (node2.getX() + Node.width * node2.getScale() / 2);
        int by = (int) (node2.getY() + Node.height * node2.getScale() / 2);
        calculateConnection(ax, ay, bx, by);
    }

    // Stores a pixel as a 64-bit key, no allocations needed.
    private long key(int x, int y) {
        return (((long) x) << 32) | (y & 0xFFFFFFFFL);
    }

    /**
     * Calculate the connection, store in precomputedPixels and bake a texture.
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public void calculateConnection(int x1, int y1, int x2, int y2) {
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

        bakeConnectionTexture();
    }

    /**
     * Add border pixels to precomputedPixels
     *
     * @param x
     * @param y
     * @param white
     * @param color
     */
    private void calculateBorderPixel(int x, int y, Set<Long> white, int color) {
        long k = key(x, y);
        if (!white.contains(k)) {
            precomputedPixels.add(new Pixel(x, y, color));
        }
    }

    public void renderPrecomputedConnection(GuiGraphics g, int scrollX, int scrollY) {
        if (!isTextureReady) return;

        int drawX = texOriginX - scrollX;
        int drawY = texOriginY - scrollY;

        g.blit(
                bakedTextureLocation,
                drawX,
                drawY,
                0, 0,
                texWidth,
                texHeight,
                texWidth,
                texHeight
        );
    }

    public boolean hasCalculatedConnections() {
        return !precomputedPixels.isEmpty();
    }

    /**
     * Bake pixels into a dynamic texture.
     */
    public void bakeConnectionTexture() {
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

        texWidth = maxX - minX + 1;
        texHeight = maxY - minY + 1;

        texOriginX = minX;
        texOriginY = minY;

        // Create dynamic texture
        DynamicTexture bakedTexture = new DynamicTexture(texWidth, texHeight, true);
        bakedTextureLocation = Minecraft.getInstance().getTextureManager()
                .register("immersive_enchanting_connection_" + hashCode(), bakedTexture);

        NativeImage image = bakedTexture.getPixels();

        // fill with transparent
        image.fillRect(0, 0, texWidth, texHeight, 0x00000000);

        // draw all pixels into texture
        for (Pixel p : precomputedPixels) {
            int px = p.x() - minX;
            int py = p.y() - minY;
            image.setPixelRGBA(px, py, p.color());
        }

        bakedTexture.upload(); // upload to GPU
        isTextureReady = true;
    }
}
