package me.alfie.immersiveenchanting.util;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class EnchantmentTextureHelper {
    /**
     * Returns the texture ResourceLocation for the given enchantment's node icon.
     * Looks for {@code immersiveenchanting:textures/enchantment/<namespace>/<path>.png};
     * falls back to the generic {@code ancient_book.png} if no custom texture is found.
     */
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
