package me.alfie.immersiveenchanting.datapack.mod_icons;

import com.mojang.serialization.Codec;
import me.alfie.immersiveenchanting.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public record ModIconsMap(Map<String, ResourceLocation> map) {

    public static final Codec<ModIconsMap> CODEC =
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC)
                    .xmap(ModIconsMap::new, ModIconsMap::map);

    public static final StreamCodec<RegistryFriendlyByteBuf, ModIconsMap> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(
                            HashMap::new,
                            ByteBufCodecs.STRING_UTF8,
                            ResourceLocation.STREAM_CODEC
                    ),
                    ModIconsMap::map,
                    ModIconsMap::new
            );

    /**
     * Returns the id as an item stack. If there is none defined, falls back to ancient book.
     *
     * @param modid
     * @return
     */
    public ItemStack getAsItemStack(String modid) {
        ResourceLocation id = map().get(modid);
        if (id == null) return new ItemStack(ModItems.ANCIENT_BOOK.get());

        return BuiltInRegistries.ITEM.get(id)
                .map(item -> new ItemStack(item, 1))
                .orElse(ItemStack.EMPTY);
    }
}
