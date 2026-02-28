package me.alfie.immersiveenchanting.datapack.cost;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public record CostGroup(List<CostDefinition> children, GroupType type, @Nullable String originalItemTag) implements CostDefinition {

    public CostGroup(List<CostDefinition> children, GroupType type) {
        this(children, type, null);
    }

    public Optional<String> getOriginalItemTag() {
        return Optional.ofNullable(originalItemTag);
    }

    @Override
    public CostDefinition resolveTags() {
        List<CostDefinition> resolvedChildren =
                children.stream()
                        .map(CostDefinition::resolveTags)
                        .toList();

        return new CostGroup(resolvedChildren, type, originalItemTag);
    }
}
