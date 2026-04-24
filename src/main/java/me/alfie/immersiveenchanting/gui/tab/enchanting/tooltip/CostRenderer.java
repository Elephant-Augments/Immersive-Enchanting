package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import me.alfie.immersiveenchanting.config.ClientConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.Cost;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostHolder;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class CostRenderer {

    private CostHolder holder;
    private CostHolder fuelHolder;

    private List<RenderedCost> renderedCosts;
    private List<RenderedCost> renderedFuelCosts;

    private final CostRegistry costRegistry;

    public CostRenderer(CostRegistry costRegistry) {
        this.costRegistry = costRegistry;
    }

    public void setCostToRender(Identifier id, int level) {
        holder = costRegistry.get(id).levelCosts().getLevel(level);
        this.renderedCosts = getRenderedCosts(holder);

        fuelHolder = costRegistry.get(CostRegistry.ENCHANTING_FUELS).levelCosts().getLevel(level);
        this.renderedFuelCosts = getRenderedCosts(fuelHolder);
    }

    private List<RenderedCost> getRenderedCosts(CostHolder holder) {
        List<RenderedCost> entries = new ArrayList<>();

        for(Cost cost : holder.costs()) {
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

        final int CAROUSEL_SPEED = ClientConfig.getItemCarouselSpeed();

        long currentTime = System.currentTimeMillis();
        int index = (int)((currentTime / CAROUSEL_SPEED) % list.size());

        return list.get(index);
    }
}
