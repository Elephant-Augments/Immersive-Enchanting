package me.alfie.immersiveenchanting.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentTextureHelper {
    /**
     * Returns the texture identifier for the given enchantment's node icon.
     * Looks for {@code immersiveenchanting:textures/enchantment/<namespace>/<path>.png};
     * falls back to the generic {@code ancient_book.png} if no custom texture is found.
     */
    public static Identifier getTexture(Identifier enchantmentId) {
        String directory = "textures/enchantment/" + enchantmentId.getNamespace() + '/' + enchantmentId.getPath() + ".png";
        Identifier iconId = Identifier.fromNamespaceAndPath("immersiveenchanting", directory);

        return Minecraft.getInstance().getResourceManager()
                .getResource(iconId)
                .isPresent()
                ? iconId
                : Identifier.fromNamespaceAndPath("immersiveenchanting", "textures/item/ancient_book.png");
    }
}
