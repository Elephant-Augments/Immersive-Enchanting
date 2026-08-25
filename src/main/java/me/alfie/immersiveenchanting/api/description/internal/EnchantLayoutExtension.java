package me.alfie.immersiveenchanting.api.description.internal;

import me.alfie.immersiveenchanting.api.description.DescriptionHelper;
import me.alfie.immersiveenchanting.api.description.DescriptionLayout;
import me.alfie.immersiveenchanting.api.description.DescriptionLayoutExtension;
import me.alfie.immersiveenchanting.api.description.internal.lines.*;
import me.alfie.immersiveenchanting.api.node.internal.EnchantmentNodeData;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import me.alfie.immersiveenchanting.util.CostHelper;
import me.alfie.immersiveenchanting.util.EnchantmentDescriptionHelper;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Populates the tooltip description for enchantment nodes based on their current state:
 * <ul>
 *   <li>UNOBTAINED – shows the enchantment cost (materials, fuel, XP levels)</li>
 *   <li>OBTAINED – shows "Equipped" or, while the remove key is held, a removal progress bar</li>
 *   <li>LOCKED – shows "Unavailable Enchantment"</li>
 * </ul>
 * No-ops for non-enchantment nodes.
 */
public class EnchantLayoutExtension implements DescriptionLayoutExtension {

    @Override
    public void extendLayout(DescriptionLayout description, NodeTooltip tooltip) {
        if(!tooltip.node().isDataType(EnchantmentNodeData.TYPE)) return;

        description.widthPadding = 16;

        Node node = tooltip.node();
        EnchantingTableScreen screen = tooltip.screen();

        if(node.isState(NodeState.UNOBTAINED)) {
            int linesCreated = insertEnchantmentDescription(description, tooltip, 0);
            if(isTooExpensive(tooltip)) {
                description.insertLine(linesCreated, new TooExpensiveLine(tooltip));
            } else {
                DescriptionHelper.insertCostLines(tooltip, description, linesCreated);
            }
        } else if(node.isState(NodeState.OBTAINED)) {
            if(screen.tooltipManager().isHoldingTooltip()) {
                description.insertLine(0, new RemovingLine(tooltip));
                description.insertLine(1, new RemoveProgressLine(tooltip));
            } else {
                int linesCreated = insertEnchantmentDescription(description, tooltip, 0);
                description.insertLine(linesCreated, new EquippedLine(tooltip));

                if(tooltip.node().canRemove()) {
                    description.insertLine(linesCreated + 1, new RemoveHintLine(tooltip));
                }

            }
        } else if(node.isState(NodeState.LOCKED)) {
            description.insertLine(0, new UnavailableEnchantmentLine(tooltip));
        }
    }

    private static int insertEnchantmentDescription(DescriptionLayout description, NodeTooltip tooltip, int lineStart) {
        if(!(tooltip.node().data().value() instanceof EnchantmentNodeData enchantmentData)) {
            return lineStart;
        }

        Holder<Enchantment> holder = EnchantmentUtil.toHolder(enchantmentData.enchantmentId(), tooltip.screen().registryAccess());
        Component descriptionText = EnchantmentDescriptionHelper.getDescription(holder);
        if(descriptionText == null) {
            return lineStart;
        }

        return lineStart + DescriptionHelper.lineWrapComponent(
                descriptionText,
                DescriptionHelper.DEFAULT_LINE_WIDTH,
                description,
                lineStart
        );
    }

    private static boolean isTooExpensive(NodeTooltip tooltip) {
        if(!(tooltip.node().data().value() instanceof EnchantmentNodeData enchantmentData)) {
            return false;
        }
        EnchantingTableScreen screen = tooltip.screen();
        return CostHelper.isTooExpensiveClient(
                screen.getMenu().getToolSlot().getItem(),
                enchantmentData.enchantmentId(),
                enchantmentData.level(),
                net.minecraft.client.Minecraft.getInstance().player
        );
    }
}
