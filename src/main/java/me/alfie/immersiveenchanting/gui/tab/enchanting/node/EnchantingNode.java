package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.gui.ScrollableCanvas;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class EnchantingNode extends Node {

    public EnchantingNode(ScrollableCanvas canvas,
                          NodeState state,
                          NodeTier tier,
                          @NotNull Identifier iconTexture) {
        super(canvas, state, tier);
        if(isState(NodeState.LOCKED)) iconTexture = null;
        setIconTexture(iconTexture);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.render(graphics, mouseX, mouseY);
    }


}
