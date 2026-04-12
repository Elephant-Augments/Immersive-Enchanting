package me.alfie.immersiveenchanting.datapack.node_sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Map;

public record NodeSoundMap(Map<Identifier, NodeSound> enchantments) {

    public static final Codec<NodeSoundMap> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Identifier.CODEC, NodeSound.CODEC).fieldOf("enchantments").forGetter(NodeSoundMap::enchantments)
    ).apply(instance, NodeSoundMap::new));
}
