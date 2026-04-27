package me.alfie.immersiveenchanting.util;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class EnchantmentTextureHelper {

    public static ResourceLocation getTexture(ResourceLocation enchantmentId) {
        String directory = "textures/enchantment/" + enchantmentId.getNamespace() + '/' + enchantmentId.getPath() + ".png";
        ResourceLocation iconId = ResourceLocation.fromNamespaceAndPath("immersiveenchanting", directory);

        return Minecraft.getInstance().getResourceManager()
                .getResource(iconId)
                .isPresent()
                ? iconId
                : ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "textures/item/ancient_book.png");
    }
}
