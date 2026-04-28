package me.alfie.immersiveenchanting.api.datapack;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

/**
 *
 * @param <A> Codec used in apply
 * @param <B> The object returned after apply is finished
 */
public abstract class ModDatapack<A, B> extends SimpleJsonResourceReloadListener<A> {

    private DatapackKey<B> key;
    private StreamCodec<RegistryFriendlyByteBuf, B> streamCodec;

    protected ModDatapack(Codec<A> codec, DatapackKey<B> datapackKey, StreamCodec<RegistryFriendlyByteBuf, B> streamCodec) {
        super(codec, FileToIdConverter.json(datapackKey.directory()));
        this.key = datapackKey;
        this.streamCodec = streamCodec;
    }

    public StreamCodec<RegistryFriendlyByteBuf, B> codec() {
        return streamCodec;
    }

    public DatapackKey<B> key() {
        return key;
    }

    public abstract B getData();

    @Override
    protected void apply(Map<Identifier, A> input, ResourceManager resourceManager, ProfilerFiller profilerFiller) {}


    /**
     * Execute code after the data has been pulled by ServerDatapackManager.
     * At this point it is safe to call ServerDatapackManager.get(DatapackKey)
     * */
    public void afterPull(MinecraftServer server) {}
}
