package me.alfie.immersiveenchanting.networking;

import me.alfie.alfinolib.networking.NetworkPacket;
import me.alfie.alfinolib.networking.codec.StreamCodec;
import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

/**
 * Client-to-server packet requesting removal starting at a held enchantment node.
 *
 * <p>Removal walks outward from the held node:
 * <ul>
 *   <li>Multi-level: reduces the enchantment to {@code level - 1}, clearing the held node and
 *       every higher equipped level.</li>
 *   <li>Sibling forks in dependency chains that do not depend on the held node are left alone.</li>
 * </ul>
 *
 * <p>No action is taken if removal is disabled, the enchantment is missing, or the held level is
 * above what is currently equipped.</p>
 */
public record RemoveEnchantmentPacket(ResourceKey<Enchantment> enchantmentKey, int level) implements NetworkPacket<RemoveEnchantmentPacket> {

    public static final Type<@NotNull RemoveEnchantmentPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "remove_enchantment"));
    @Override public Type<@NotNull RemoveEnchantmentPacket> type() {
        return TYPE;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, RemoveEnchantmentPacket> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, RemoveEnchantmentPacket>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, RemoveEnchantmentPacket packet) {
            ModPackets.ENCHANTMENT_CODEC.encode(buf, packet.enchantmentKey);
            buf.writeInt(packet.level);
        }

        @Override
        public RemoveEnchantmentPacket decode(RegistryFriendlyByteBuf buf) {
            return new RemoveEnchantmentPacket(ModPackets.ENCHANTMENT_CODEC.decode(buf), buf.readInt());
        }
    };

    @Override
    public void exec(IPayloadContext context) {
        Player player = context.player();
        if (!(player.containerMenu instanceof EnchantingTableMenu menu)) return;
        if(!ServerConfig.isEnchantmentRemovalAllowed()) return;

        RegistryAccess lookup = player.registryAccess();
        Holder<Enchantment> heldHolder = EnchantmentUtil.toHolder(enchantmentKey, lookup);

        ItemStack stack = menu.getToolSlot().getItem();
        ItemEnchantments present = EnchantmentUtil.getEnchantments(stack);
        int currentLevel = present.getLevel(heldHolder);
        if (currentLevel <= 0 || currentLevel < level) return;

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(present);
        int newLevel = level - 1;
        if(newLevel > 0) {
            mutable.set(heldHolder, newLevel);
        } else {
            mutable.removeIf(e -> e.equals(heldHolder));
            removeOutwardDependents(heldHolder, mutable, CostRegistry.server(), lookup);
        }

        EnchantmentHelper.setEnchantments(stack, mutable.toImmutable());

        menu.getToolSlot().set(EnchantmentUtil.tryConvertVanillaBook(stack));

        FxHelper.playEnchantmentRemove(context.player().level(), menu.getBlockPos());
    }

    /**
     * Fully removes every obtained enchantment that depends on {@code root}. 
     * Only walks outward through {@code depends_on} children, so a sibling
     * fork that depends on a shared ancestoris preserved.
     */
    private static void removeOutwardDependents(
            Holder<Enchantment> root,
            ItemEnchantments.Mutable mutable,
            CostRegistry registry,
            RegistryAccess lookup
    ) {
        Set<ResourceId> visited = new HashSet<>();
        Queue<ResourceId> queue = new ArrayDeque<>();
        queue.add(EnchantmentUtil.toId(root));

        while(!queue.isEmpty()) {
            ResourceId parentId = queue.poll();
            if(!visited.add(parentId)) {
                continue;
            }

            List<ResourceId> children = registry.dependencies().getChildren(parentId);
            for(ResourceId childId : children) {
                Optional<Holder.Reference<Enchantment>> child = lookup.lookupOrThrow(Registries.ENCHANTMENT)
                        .get(ResourceKey.create(Registries.ENCHANTMENT, childId.mc()));
                if(child.isEmpty() || mutable.getLevel(child.get()) <= 0) {
                    continue;
                }
                mutable.removeIf(e -> e.equals(child.get()));
                queue.add(childId);
            }
        }
    }
}
