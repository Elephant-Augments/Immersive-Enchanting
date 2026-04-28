package me.alfie.immersiveenchanting.api.datapack;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record DatapackKey<T>(String modid, String directory) {

    public static final StreamCodec<RegistryFriendlyByteBuf, DatapackKey<?>> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, DatapackKey::modid,
                    ByteBufCodecs.STRING_UTF8, DatapackKey::directory,
                    DatapackKey::new
            );
}
