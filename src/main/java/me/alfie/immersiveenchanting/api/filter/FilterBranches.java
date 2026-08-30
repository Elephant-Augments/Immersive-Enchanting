package me.alfie.immersiveenchanting.api.filter;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.node.BuildBranchesEvent;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Registry for {@link GlobalFilterBranch} and {@link IsolatedFilterBranch} contributions.
 *
 * <p>Built-in IE filters are registered via {@link RegisterFilterBranchesEvent}. 
 * Other mods can register additional filters on the same event.</p>
 */
public final class FilterBranches {

    private static final List<GlobalFilterBranch> GLOBAL = new ArrayList<>();
    private static final List<IsolatedFilterBranch> ISOLATED = new ArrayList<>();

    private FilterBranches() {}

    public static void registerGlobal(GlobalFilterBranch branch) {
        Objects.requireNonNull(branch, "branch");
        GLOBAL.add(branch);
        ImmersiveEnchanting.LOGGER.debug("Registered GlobalFilterBranch {}", branch.id());
    }

    public static void registerIsolated(IsolatedFilterBranch branch) {
        Objects.requireNonNull(branch, "branch");
        ISOLATED.add(branch);
        ImmersiveEnchanting.LOGGER.debug("Registered IsolatedFilterBranch {}", branch.id());
    }

    public static List<GlobalFilterBranch> global() {
        return Collections.unmodifiableList(GLOBAL);
    }

    public static List<IsolatedFilterBranch> isolated() {
        return Collections.unmodifiableList(ISOLATED);
    }

    public static IsolatedFilterBranch findIsolated(ResourceId id) {
        for (IsolatedFilterBranch branch : ISOLATED) {
            if (branch.id().equals(id) || branch.pickerBranchId().equals(id)) {
                return branch;
            }
        }
        return null;
    }

    /**
     * @return {@code true} if the enchantment belongs to any registered isolated tag.
     */
    public static boolean isInAnyIsolatedTag(Holder<Enchantment> enchantment) {
        for (IsolatedFilterBranch branch : ISOLATED) {
            if (branch.contains(enchantment)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds all registered global + isolated picker branches for hold-to-filter mode.
     */
    public static void addPickerBranches(BuildBranchesEvent event, List<Holder<Enchantment>> applicableEnchantments) {
        EnchantmentFilterSelection selection = event.getCanvas().screen().enchantmentFilterSelection();

        for (GlobalFilterBranch branch : GLOBAL) {
            branch.addPickerBranches(event, applicableEnchantments);
        }

        for (IsolatedFilterBranch branch : ISOLATED) {
            if (branch.shouldShow(applicableEnchantments)) {
                boolean selected = selection instanceof EnchantmentFilterSelection.Isolated isolated
                        && (branch.id().equals(isolated.branchId())
                                || branch.pickerBranchId().equals(isolated.branchId()));
                branch.addPickerBranch(event, selected);
            }
        }
    }

    /**
     * Whether the enchantment should appear under the current filter selection.
     *
     * @param unlockedTest typically {@link EnchantingTableMenu#isEnchantmentAvailable}
     */
    public static boolean matches(
        EnchantmentFilterSelection selection, 
        Holder<Enchantment> enchantment, 
        Predicate<Holder<Enchantment>> unlockedTest
    ) {
        if(selection instanceof EnchantmentFilterSelection.Search) {
            return true;
        }

        if (selection instanceof EnchantmentFilterSelection.Isolated isolated) {
            IsolatedFilterBranch branch = findIsolated(isolated.branchId());
            return branch != null && branch.contains(enchantment);
        }

        if (isInAnyIsolatedTag(enchantment)) {
            return false;
        }

        if (selection instanceof EnchantmentFilterSelection.Unlocked) {
            return unlockedTest.test(enchantment);
        }
        if (selection instanceof EnchantmentFilterSelection.ModNamespace mod) {
            String namespace = enchantment.unwrapKey()
                    .map(key -> key.location().getNamespace())
                    .orElse(null);
            return mod.modid().equals(namespace);
        }
        return true;
    }
}
