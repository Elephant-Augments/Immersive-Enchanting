package me.alfie.immersiveenchanting.datapack.node_sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.alfie.immersiveenchanting.api.datapack.internal.DatapackKeys;
import me.alfie.immersiveenchanting.api.datapack.manager.ClientDatapackManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record NodeSoundMap(Map<ResourceLocation, NodeSound> enchantments) {

    public static final Codec<NodeSoundMap> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(ResourceLocation.CODEC, NodeSound.CODEC).fieldOf("enchantments").forGetter(NodeSoundMap::enchantments)
    ).apply(instance, NodeSoundMap::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, NodeSoundMap> STREAM_CODEC =
            StreamCodec.of(
                    NodeSoundMap::encode,
                    NodeSoundMap::decode
            );

    public static NodeSoundMap client() {
        return ClientDatapackManager.get(DatapackKeys.NODE_SOUNDS);
    }

    private static void encode(RegistryFriendlyByteBuf buf, NodeSoundMap map) {
        buf.writeInt(map.enchantments.size());

        for (var entry : map.enchantments.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            NodeSound.STREAM_CODEC.encode(buf, entry.getValue());
        }
    }

    private static NodeSoundMap decode(RegistryFriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<ResourceLocation, NodeSound> map = new HashMap<>();

        for (int i = 0; i < size; i++) {
            ResourceLocation id = buf.readResourceLocation();
            NodeSound sound = NodeSound.STREAM_CODEC.decode(buf);
            map.put(id, sound);
        }

        return new NodeSoundMap(map);
    }

    public boolean containsKey(ResourceLocation ResourceLocation) {
        return enchantments().containsKey(ResourceLocation);
    }

    public NodeSound get(ResourceLocation id) {
        return enchantments.get(id);
    }
}
