package me.alfie.immersiveenchanting.datapack;

import java.util.List;

public record CostComposite(List<CostNode> children, CompositeType type) implements CostNode {
}
