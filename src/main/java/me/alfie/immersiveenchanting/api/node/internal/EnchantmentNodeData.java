package me.alfie.immersiveenchanting.api.node.internal;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.node.NodePayload;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.api.node.NodeData;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.networking.EnchantPacket;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public record EnchantmentNodeData(Identifier enchantmentId, int level) implements NodePayload {

    public static final Identifier TYPE = Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "enchantment");

    /**
     * Creates a {@link NodeData} for an enchantment node.
     *
     * <p>On click:
     * <ul>
     *   <li>If the node is OBTAINED and {@link me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node#canRemove()}
     *       is true, starts the hold-to-remove gesture.</li>
     *   <li>Otherwise sends an {@link EnchantPacket} to apply the enchantment at the given level.</li>
     * </ul>
     */
    public static NodeData<EnchantmentNodeData> create(Identifier enchantmentId, int level) {
        return new NodeData<>(
                TYPE,
                new EnchantmentNodeData(enchantmentId, level),
                (data, context) -> {
                    Node node = context.node();
                    EnchantingTableScreen screen = context.screen();

                    if (node.isState(NodeState.OBTAINED) && node.canRemove()) {
                        screen.tooltipManager().startHold(node);
                    } else {
                        Holder<Enchantment> enchantmentHolder = EnchantmentUtil.toHolder(data.enchantmentId(), screen.registryAccess());
                        ClientPacketDistributor.sendToServer(new EnchantPacket(enchantmentHolder, data.level()));
                    }

                }
        );
    }

    @Override
    public Identifier type() {
        return TYPE;
    }
}
