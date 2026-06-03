package me.alfie.immersiveenchanting.datapack.mod_icons;

import com.mojang.serialization.Codec;
import me.alfie.alfinolib.networking.codec.StreamCodec;
import me.alfie.immersiveenchanting.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public record ModIconsMap(Map<String, Identifier> map) {

    public static final Codec<ModIconsMap> CODEC =
            Codec.unboundedMap(Codec.STRING, Identifier.CODEC)
                    .xmap(ModIconsMap::new, ModIconsMap::map);


    public static final StreamCodec<RegistryFriendlyByteBuf, ModIconsMap> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ModIconsMap>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, ModIconsMap modIconsMap) {
            Map<String, Identifier> map = modIconsMap.map();

            buf.writeVarInt(map.size());

            for (Map.Entry<String, Identifier> entry : map.entrySet()) {
                buf.writeUtf(entry.getKey());
                Identifier.STREAM_CODEC.encode(buf, entry.getValue());
            }
        }

        @Override
        public ModIconsMap decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();

            Map<String, Identifier> map = new HashMap<>(size);

            for (int i = 0; i < size; i++) {
                String key = buf.readUtf();
                Identifier value = Identifier.STREAM_CODEC.decode(buf);

                map.put(key, value);
            }

            return new ModIconsMap(map);
        }
    };

    /**
     * Returns the id as an item stack. If there is none defined, falls back to ancient book.
     * @param modid
     * @return
     */
    public ItemStack getAsItemStack(String modid) {
        Identifier id = map().get(modid);
        if (id == null) return new ItemStack(ModItems.ANCIENT_BOOK.get());

        return BuiltInRegistries.ITEM.get(id)
                .map(item -> new ItemStack(item, 1))
                .orElse(ItemStack.EMPTY);
    }
}
