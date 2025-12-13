package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.enums.EnchantingNodeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import org.joml.Vector2i;

public class EnchantingNode {
    private int x, y;
    private final EnchantingNodeType node_type;
    static final int width = 26; //Texture size
    static final int height = 26; //Texture size
    private boolean obtained;
    private final ResourceLocation icon_texture;
    private final int enchantmentLevel;
    private final ResourceKey<Enchantment> enchantment;
    private final Holder<Enchantment> enchantmentHolder;
    final boolean isBranchUnlocked;
    private float scale = 1;
    public static float globalScale = 1.0f;


    private final ResourceLocation LOCKED_ICON = ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "textures/gui/enchantment_icons/locked_enchantment.png");

    public EnchantingNode(EnchantingNodeType node_type, ResourceLocation icon_texture, int enchantmentLevel,
                          Holder<Enchantment> enchantmentHolder,
                          boolean isBranchUnlocked) {
        this.isBranchUnlocked = isBranchUnlocked;
        this.enchantmentLevel = enchantmentLevel;
        this.enchantmentHolder = enchantmentHolder;
        this.enchantment = enchantmentHolder.unwrapKey().get();

        if (isBranchUnlocked) {
            this.node_type = node_type;
            this.icon_texture = icon_texture;
        } else {
            this.node_type = EnchantingNodeType.LOCKED;
            //this.icon_texture = LOCKED_ICON;
            this.icon_texture = null;
        }
    }

    public ResourceLocation getCurrentTexture() {
        return obtained ? node_type.getObtainedTexture() : node_type.getUnobtainedTexture();
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }

    public boolean isInViewport(double scrollX, double scrollY, int viewportWidth, int viewportHeight) {
        return getX() + width >= scrollX &&
                getX() <= scrollX + viewportWidth &&
                getY() + height >= scrollY &&
                getY() <= scrollY + viewportHeight;
    }

    public void render(GuiGraphics guiGraphics, EnchantingTableScreen screen) {
        guiGraphics.pose().pushPose();

        guiGraphics.pose().translate(this.x - (int)screen.scrollX, this.y - (int)screen.scrollY, 0);
        guiGraphics.pose().scale(scale, scale, scale);

        guiGraphics.blit(
                this.getCurrentTexture(),
                0,
                0,
                0f, 0f,
                width, height,
                width, height
        );

        //Blit book texture
        if(icon_texture != null) {
            guiGraphics.blit(
                    icon_texture,
                    4,
                    4,
                    0f, 0f,
                    16, 16,
                    16, 16
            );
        }
        guiGraphics.pose().popPose();

    }


    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }


    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public boolean isMouseOver(double mouseX, double mouseY, EnchantingTableScreen screen) {
        // Node's position on screen
        int drawX = this.x - (int) screen.scrollX;
        int drawY = this.y - (int) screen.scrollY;

        // Node bounds
        int nodeLeft   = drawX;
        int nodeTop    = drawY;
        int nodeRight  = (int) (drawX + width*scale);
        int nodeBottom = (int) (drawY + height*scale);

        // Viewport bounds
        int viewportLeft   = screen.canvasLeftPos;
        int viewportTop    = screen.canvasTopPos;
        int viewportRight  = viewportLeft + screen.viewportWidth;
        int viewportBottom = viewportTop + screen.viewportHeight;

        // Clip node bounds to viewport
        int visibleLeft   = Math.max(nodeLeft, viewportLeft);
        int visibleTop    = Math.max(nodeTop, viewportTop);
        int visibleRight  = Math.min(nodeRight, viewportRight);
        int visibleBottom = Math.min(nodeBottom, viewportBottom);

        // If the node is fully outside the viewport, return false
        if (visibleLeft >= visibleRight || visibleTop >= visibleBottom) return false;

        // Check if mouse is over the visible part
        return mouseX >= visibleLeft && mouseX < visibleRight
                && mouseY >= visibleTop && mouseY < visibleBottom;
    }



    public int getEnchantmentLevel() {
        return enchantmentLevel;
    }

    public Holder<Enchantment> getEnchantmentHolder() {
        return enchantmentHolder;
    }

    public ResourceKey<Enchantment> getEnchantment() {
        return enchantment;
    }

    public void setObtained(boolean obtained) {
        this.obtained = obtained;
    }

    public boolean isObtained() {
        return this.obtained;
    }

    public Vector2i getRenderedPosition(EnchantingTableScreen screen) {
        int renderedX = this.x - (int) screen.scrollX;
        int renderedY = this.y - (int) screen.scrollY;
        return new Vector2i(renderedX, renderedY);
    }

    public Vector2i getViewportPosition(EnchantingTableScreen screen) {
        int screenX = this.x - (int) screen.scrollX;
        int screenY = this.y - (int) screen.scrollY;

        int viewportX = screenX - screen.canvasLeftPos;
        int viewportY = screenY - screen.canvasTopPos;

        return new Vector2i(viewportX, viewportY);
    }

}
