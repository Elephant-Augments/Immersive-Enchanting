package me.alfie.immersiveenchanting.api.datapack;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Unique ResourceLocation for a datapack and its associated data type.
 *
 * <p>A {@code DatapackKey<T>} serves two purposes:
 * <ul>
 *     <li>Identifies a datapack by its {@code modid} and {@code directory}</li>
 *     <li>Defines the expected type ({@code T}) of the data it provides</li>
 * </ul>
 *
 * <p>This key is used throughout the API to:
 * <ul>
 *     <li>Register datapacks</li>
 *     <li>Retrieve processed data from {@link DataMap}</li>
 *     <li>Look up the corresponding {@link ModDatapack}</li>
 * </ul>
 *
 * <p>Example:
 * <pre>
 * DatapackKey&lt;CostRegistry&gt; COST = new DatapackKey<>("modid", "enchantment_costs");
 * </pre>
 *
 * <p>Note: The generic type {@code T} is not present at runtime (type erasure),
 * but is used at compile time for type safety when retrieving data.</p>
 *
 * @param modid     the mod ID that owns this datapack
 * @param directory the datapack directory (inside {@code data/&lt;modid&gt;/})
 * @param <T>       the type of data this datapack provides
 */
public record DatapackKey<T>(String modid, String directory) {

    /**
     * Stream codec used to serialize and deserialize {@link DatapackKey}.
     *
     * <p>This only encodes the structural identity ({@code modid + directory}).
     * The generic type {@code T} is not serialized and must be known by both
     * sides implicitly via registration.</p>
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, DatapackKey<?>> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, DatapackKey::modid,
                    ByteBufCodecs.STRING_UTF8, DatapackKey::directory,
                    DatapackKey::new
            );

    public ResourceLocation ResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(modid, directory);
    }
}
