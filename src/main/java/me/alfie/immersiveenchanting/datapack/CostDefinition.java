package me.alfie.immersiveenchanting.datapack;

public sealed interface CostDefinition permits CostEntry, CostGroup {
    CostDefinition resolveTags();
}
