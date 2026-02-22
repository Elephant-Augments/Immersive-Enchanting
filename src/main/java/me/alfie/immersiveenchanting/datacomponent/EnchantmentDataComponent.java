package me.alfie.immersiveenchanting.datacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * Holds the enchantment resource location as a string, for example "minecraft:respiration"
 * @deprecated Use DataComponents.StoredEnchantment
 * @param enchantmentResourceLocation
 */
@Deprecated
public record EnchantmentDataComponent(String enchantmentResourceLocation) {

    static final Codec<EnchantmentDataComponent> BASIC_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("value1").forGetter(EnchantmentDataComponent::enchantmentResourceLocation)
            ).apply(instance, EnchantmentDataComponent::new)
    );

    static final StreamCodec<ByteBuf, EnchantmentDataComponent> BASIC_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, EnchantmentDataComponent::enchantmentResourceLocation,
            EnchantmentDataComponent::new
    );

    /**
     * Return the enchantment resource location for an item stack (if it has one).
     * @param stack
     * @return
     */
    public static String getEnchantmentData(ItemStack stack) {
        EnchantmentDataComponent dataComponent = stack.get(ModDataComponents.LEGACY_ENCHANTMENT.get());
        if (dataComponent != null) {
            return dataComponent.enchantmentResourceLocation();
        }
        return null;


    }
}