package me.alfie.immersiveenchanting.networking;

import me.alfie.alfinolib.networking.NetworkPacket;
import me.alfie.alfinolib.networking.codec.StreamCodec;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Client-to-server packet requesting removal of an enchantment position.
 *
 * <p>If allowed by server configuration, reduces the specified enchantment by one position.
 * If the resulting position reaches zero, the enchantment is fully removed from the item.</p>
 *
 * <p>No action is taken if:
 * <ul>
 *     <li>Enchantment removal is disabled in config</li>
 *     <li>The item has no matching enchantment</li>
 *     <li>The enchantment position is already zero</li>
 * </ul>
 *
 * <p>On success, the item is updated server-side and feedback effects are triggered.</p>
 */
public record RemoveEnchantmentPacket(ResourceKey<Enchantment> enchantmentKey, int level) implements NetworkPacket<RemoveEnchantmentPacket> {

    public static final Type<@NotNull RemoveEnchantmentPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "remove_enchantment"));
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

        Holder<Enchantment> enchantmentHolder = EnchantmentUtil.toHolder(enchantmentKey,
                player.registryAccess());

        ItemStack stack = menu.getToolSlot().getItem();
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(stack.getTagEnchantments());

        int currentLevel = mutable.getLevel(enchantmentHolder);
        if (currentLevel <= 0) return;

        int newLevel = currentLevel - 1;
        if (newLevel > 0) {
            mutable.set(enchantmentHolder, newLevel);
        } else {
            mutable.removeIf(e -> e.equals(enchantmentHolder));
        }

        EnchantmentHelper.setEnchantments(stack, mutable.toImmutable());
        FxHelper.playEnchantmentRemove(context.player().level(), menu.getBlockPos());
    }
}
