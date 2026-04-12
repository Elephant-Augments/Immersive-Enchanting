package me.alfie.immersiveenchanting.api.description;

import me.alfie.immersiveenchanting.datapack.enchantment_cost.EnchantmentCost;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.EnchantmentCostHolder;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.RenderedCost;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class EnchantmentCostRenderer {

    private EnchantmentCostHolder holder;
    private EnchantmentCostHolder fuelHolder;

    private List<RenderedCost> renderedCosts;
    private List<RenderedCost> renderedFuelCosts;

    public static long CAROUSEL_SPEED = 700;

    public EnchantmentCostRenderer() {

    }

    public void setCostToRender(Identifier id, int level) {
        holder = EnchantmentCostRegistry.get(id).levelCosts().getLevel(level);
        this.renderedCosts = getRenderedCosts(holder);

        fuelHolder = EnchantmentCostRegistry.get(EnchantmentCostRegistry.ENCHANTING_FUELS).levelCosts().getLevel(level);
        this.renderedFuelCosts = getRenderedCosts(fuelHolder);
    }

    private List<RenderedCost> getRenderedCosts(EnchantmentCostHolder holder) {
        List<RenderedCost> entries = new ArrayList<>();

        for(EnchantmentCost cost : holder.costs()) {
            for(ItemStack stack : cost.getItemStacks()) {
                entries.add(new RenderedCost(stack, cost.xpLevels()));
            }
        }

        return entries;
    }

    public RenderedCost getCurrentRenderedCost() {
        return getCycledElement(renderedCosts);
    }

    public RenderedCost getCurrentRenderedFuel() {
        return getCycledElement(renderedFuelCosts);
    }

    /**
     * Returns the currently active element from a list, cycling through it
     * based on system time and a given interval in milliseconds.
     *
     * @param <T> the type of elements
     * @param list the list of elements to cycle through
     * @return the current element
     */
    public static <T> T getCycledElement(List<T> list) {
        if (list == null || list.isEmpty()) return null;

        long currentTime = System.currentTimeMillis();
        int index = (int)((currentTime / CAROUSEL_SPEED) % list.size());

        return list.get(index);
    }
}
