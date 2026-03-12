package me.alfie.immersiveenchanting.datapack.cost;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public record CostGroup(List<CostDefinition> children, GroupType type, @Nullable CostItemTag costItemTag) implements CostDefinition {

    public CostGroup(List<CostDefinition> children, GroupType type) {
        this(children, type, null);
    }

    public Optional<CostItemTag> getCostItemTag() {
        return Optional.ofNullable(costItemTag());
    }
}
