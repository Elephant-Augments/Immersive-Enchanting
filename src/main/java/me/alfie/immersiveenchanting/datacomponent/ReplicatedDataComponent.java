package me.alfie.immersiveenchanting.datacomponent;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record ReplicatedDataComponent(boolean isReplicated) {

    static final Codec<ReplicatedDataComponent> BASIC_CODEC =
            Codec.BOOL.xmap(
                    ReplicatedDataComponent::new,
                    ReplicatedDataComponent::isReplicated
            );

    static final StreamCodec<ByteBuf, ReplicatedDataComponent> BASIC_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ReplicatedDataComponent::isReplicated,
            ReplicatedDataComponent::new
    );

    public static boolean isReplicated(ItemStack stack) {
        //No data component - assume book is not replicated.
        if(!stack.has(ModDataComponents.REPLICATED)) return false;
        return stack.get(ModDataComponents.REPLICATED.get()).isReplicated();
    }
}