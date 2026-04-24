package me.alfie.immersiveenchanting.datapack.node_sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record NodeSound(Identifier sound, float volume, float pitch) {

    public static final Codec<NodeSound> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Identifier.CODEC.fieldOf("sound").forGetter(NodeSound::sound),
                    Codec.FLOAT.fieldOf("volume").forGetter(NodeSound::volume),
                    Codec.FLOAT.fieldOf("pitch").forGetter(NodeSound::pitch)
            ).apply(instance, NodeSound::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, NodeSound> STREAM_CODEC =
            StreamCodec.of(NodeSound::encode, NodeSound::decode);

    private static void encode(RegistryFriendlyByteBuf buf, NodeSound sound) {
        buf.writeIdentifier(sound.sound());
        buf.writeFloat(sound.volume());
        buf.writeFloat(sound.pitch());
    }

    private static NodeSound decode(RegistryFriendlyByteBuf buf) {
        return new NodeSound(
                buf.readIdentifier(),
                buf.readFloat(),
                buf.readFloat()
        );
    }
}
