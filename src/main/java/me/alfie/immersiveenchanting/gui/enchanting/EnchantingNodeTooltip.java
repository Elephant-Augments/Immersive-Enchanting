package me.alfie.immersiveenchanting.gui.enchanting;

import me.alfie.immersiveenchanting.datapack.cost.CostEntry;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.NodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class EnchantingNodeTooltip extends NodeTooltip {

    private final int costIconSize = 16;
    private final List<CostEntry> validCosts;
    private CostEntry currentRenderedCost;
    private Vector2i costStackPos = new Vector2i(0, 0);
    public List<Component> stackDescriptionComponents = new ArrayList<>() {{add(Component.empty());}};

    public EnchantingNodeTooltip(EnchantingNode node,
                                 List<CostEntry> validCosts,
                                 EnchantingTableScreen screen) {
        super(node, screen);
        this.validCosts = validCosts;

        //Decide title text
        String titleText;
        if (node.isBranchUnlocked) {
            titleText = Enchantment.getFullname(node.getEnchantmentHolder(), node.getEnchantmentLevel())
                    .copy() // creates a mutable copy
                    .withStyle(ChatFormatting.WHITE)
                    .getString();
        } else {
            titleText = Component.translatable("gui.immersiveenchanting.locked_enchantment").withStyle(ChatFormatting.RED).getString();
        }

        tooltipTitle.setTitleText(titleText);
    }

    public List<CostEntry> getValidCosts() {
        return this.validCosts;
    }

    public void setCostStackPos(int x, int y) {
        costStackPos = new Vector2i(x, y);
    }

    public Vector2i getCostStackPos() {
        return costStackPos;
    }

    private ItemStack getCostStack(int index) {
        return validCosts.get(index).asItemStack();
    }

    /**
     * Returns the currently active element from a list, cycling through it
     * based on system time and a given interval in milliseconds.
     *
     * @param <T> the type of elements
     * @param list the list of elements to cycle through
     * @param intervalMillis how long each element is shown before moving to the next
     * @return the current element
     */
    public static <T> T getCycledElement(List<T> list, long intervalMillis) {
        if (list == null || list.isEmpty()) return null;

        long currentTime = System.currentTimeMillis();
        int index = (int)((currentTime / intervalMillis) % list.size());

        return list.get(index);
    }

    public void setCurrentRenderedCost(CostEntry currentRenderedCost) {
        this.currentRenderedCost = currentRenderedCost;
    }

    public CostEntry getCurrentRenderedCost() {
        return currentRenderedCost;
    }
}
