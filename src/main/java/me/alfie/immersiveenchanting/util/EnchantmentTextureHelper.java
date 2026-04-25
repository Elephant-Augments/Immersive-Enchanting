package me.alfie.immersiveenchanting.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentTextureHelper {
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
