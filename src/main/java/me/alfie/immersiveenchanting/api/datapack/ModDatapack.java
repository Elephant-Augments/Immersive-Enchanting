package me.alfie.immersiveenchanting.api.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

/**
 * Base class for all Immersive Enchanting datapacks.
 *
 * <p>This class handles:
 * <ul>
 *     <li>Reading JSON files using a {@link Codec}</li>
 *     <li>Providing a structured way to process datapack data</li>
 *     <li>Supplying a {@link StreamCodec} for syncing data to the client</li>
 * </ul>
 *
 * <p>Datapacks follow a simple pipeline:
 * <pre>
 * JSON → A (via Codec) → process → B (your usable data)
 * </pre>
 *
 * <p>Where:
 * <ul>
 *     <li>{@code A} = raw data decoded from JSON</li>
 *     <li>{@code B} = processed data used by your mod at runtime</li>
 * </ul>
 *
 * <p>Example:
 * <pre>
 * ModDatapack&lt;CostData, CostRegistry&gt;
 * </pre>
 *
 * <p>Lifecycle:
 * <ol>
 *     <li>{@link #apply(Map, ResourceManager, ProfilerFiller)} is called during datapack reload</li>
 *     <li>Your implementation reads and processes data into {@code B}</li>
 *     <li>{@link me.alfie.immersiveenchanting.api.datapack.manager.ServerDatapackManager} collects all data</li>
 *     <li>{@link #afterPull(MinecraftServer)} is called for post-processing</li>
 * </ol>
 *
 * @param <A> Raw data type decoded from JSON using the {@link Codec}
 * @param <B> Final processed data type used by your mod
 */
public abstract class ModDatapack<A, B> extends SimpleJsonResourceReloadListener {

    /**
     * Unique key identifying this datapack and its output type.
     */
    private DatapackKey<B> key;

    /**
     * Stream codec used to sync {@code B} to the client.
     */
    private StreamCodec<RegistryFriendlyByteBuf, B> streamCodec;

    private Codec<A> codec;

    /**
     * @param codec       Codec used to read JSON into {@code A}
     * @param datapackKey Unique key identifying this datapack
     * @param streamCodec Codec used to sync {@code B} over the network
     */
    protected ModDatapack(Codec<A> codec, DatapackKey<B> datapackKey, StreamCodec<RegistryFriendlyByteBuf, B> streamCodec) {
        super(new Gson(), datapackKey.directory());
        this.key = datapackKey;
        this.streamCodec = streamCodec;
        this.codec = codec;
    }

    /**
     * @return the {@link StreamCodec} used to serialize this datapack's data
     */
    public StreamCodec<RegistryFriendlyByteBuf, B> codec() {
        return streamCodec;
    }

    /**
     * @return the {@link DatapackKey} for this datapack
     */
    public DatapackKey<B> key() {
        return key;
    }

    /**
     * @return the processed datapack data ({@code B})
     *
     * <p>This should return the data built during {@link #apply}.</p>
     */
    public abstract B getData();

    /**
     * Called when datapacks are loaded or reloaded.
     *
     * <p>This is where you read and process raw JSON data into your final format.</p>
     *
     * @param input           map of resource ID → decoded JSON object ({@code A})
     * @param resourceManager resource manager
     * @param profilerFiller  profiler
     */
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> input, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
    }

    public A parseOrDefault(JsonElement element, A defaultValue) {
        return codec.parse(JsonOps.INSTANCE, element)
                .resultOrPartial(error -> ImmersiveEnchanting.LOGGER.error("Failed to parse JSON for datapack {}: {}", key, error))
                .orElse(defaultValue);
    }
}
