package me.alfie.immersiveenchanting.datapack.node_sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record NodeSound(Identifier sound, float volume, float pitch) {

    public static final Codec<NodeSound> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Identifier.CODEC.fieldOf("sound").forGetter(NodeSound::sound),
                    Codec.FLOAT.fieldOf("volume").forGetter(NodeSound::volume),
                    Codec.FLOAT.fieldOf("pitch").forGetter(NodeSound::pitch)
            ).apply(instance, NodeSound::new));


}
