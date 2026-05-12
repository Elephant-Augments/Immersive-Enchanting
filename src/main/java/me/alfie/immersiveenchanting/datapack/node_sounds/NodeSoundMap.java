package me.alfie.immersiveenchanting.datapack.node_sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.alfie.immersiveenchanting.datapack.DatapackKeys;
import me.alfie.immersiveenchanting.api.datapack.manager.ClientDatapackManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public record NodeSoundMap(Map<Identifier, NodeSound> enchantments) {

    public static NodeSoundMap client() {
        return ClientDatapackManager.get(DatapackKeys.NODE_SOUNDS);
    }

    public static final Codec<NodeSoundMap> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Identifier.CODEC, NodeSound.CODEC).fieldOf("enchantments").forGetter(NodeSoundMap::enchantments)
    ).apply(instance, NodeSoundMap::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, NodeSoundMap> STREAM_CODEC =
            StreamCodec.of(
                    NodeSoundMap::encode,
                    NodeSoundMap::decode
            );

    private static void encode(RegistryFriendlyByteBuf buf, NodeSoundMap map) {
        buf.writeInt(map.enchantments.size());

        for (var entry : map.enchantments.entrySet()) {
            buf.writeIdentifier(entry.getKey());
            NodeSound.STREAM_CODEC.encode(buf, entry.getValue());
        }
    }

    private static NodeSoundMap decode(RegistryFriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<Identifier, NodeSound> map = new HashMap<>();

        for (int i = 0; i < size; i++) {
            Identifier id = buf.readIdentifier();
            NodeSound sound = NodeSound.STREAM_CODEC.decode(buf);
            map.put(id, sound);
        }

        return new NodeSoundMap(map);
    }

    public boolean containsKey(Identifier identifier) {
        return enchantments().containsKey(identifier);
    }

    public NodeSound get(Identifier id) {
        return enchantments.get(id);
    }
}
