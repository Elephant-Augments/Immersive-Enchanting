package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import me.alfie.immersiveenchanting.config.ClientConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.Cost;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class CostRenderer {

    private final CostRegistry costRegistry;
    private CostHolder holder;
    private CostHolder fuelHolder;
    private List<RenderedCost> renderedCosts;
    private List<RenderedCost> renderedFuelCosts;

    public CostRenderer(CostRegistry costRegistry) {
        this.costRegistry = costRegistry;
    }

    /**
     * Loads the cost and fuel data for the given enchantment ID and level so it can be
     * cycled and displayed in the tooltip. Must be called whenever the active node changes.
     */
    public void setCostToRender(ResourceLocation id, int level) {
        holder = Optional.ofNullable(costRegistry.get(id))
                .map(entry -> entry.levelCosts().getLevel(level))
                .orElse(CostHolder.EMPTY);

        this.renderedCosts = getRenderedCosts(holder);

        fuelHolder = costRegistry.get(CostRegistry.ENCHANTING_FUELS).levelCosts().getLevel(level);
        this.renderedFuelCosts = getRenderedCosts(fuelHolder);
    }

    /**
     * Flattens a {@link CostHolder} into a list of {@link RenderedCost} entries, one per
     * item stack variant across all costs. Used to build the carousel list.
     */
    private List<RenderedCost> getRenderedCosts(CostHolder holder) {
        List<RenderedCost> entries = new ArrayList<>();

        for (Cost cost : holder.costs()) {
            for (ItemStack stack : cost.getItemStacks()) {
                entries.add(new RenderedCost(stack, cost.xpLevels()));
            }
        }

        return entries;
    }

    public RenderedCost getCurrentRenderedCost() {
        return getCycledElement(renderedCosts);
    }

    /**
     * Returns the currently active element from a list, cycling through it
     * based on system time and a given interval in milliseconds.
     *
     * @param <T>  the type of elements
     * @param list the list of elements to cycle through
     * @return the current element
     */
    public static <T> T getCycledElement(List<T> list) {
        if (list == null || list.isEmpty()) return null;

        final int CAROUSEL_SPEED = ClientConfig.getItemCarouselSpeed();

        long currentTime = System.currentTimeMillis();
        int index = (int) ((currentTime / CAROUSEL_SPEED) % list.size());

        return list.get(index);
    }

    public RenderedCost getCurrentRenderedFuel() {
        return getCycledElement(renderedFuelCosts);
    }
}
