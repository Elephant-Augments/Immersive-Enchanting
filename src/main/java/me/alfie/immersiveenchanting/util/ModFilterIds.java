package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.resources.Identifier;

public class ModFilterIds {
    private static final String MOD_FILTER_STEM = "mod_filter/";
    public static final String ALL = "all";

    public static Identifier createModFilterId(String modid) {
        return Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, MOD_FILTER_STEM + modid);
    }

    public static String getModidFromFilterId(Identifier id) {
        String modid = id.getPath().replace(MOD_FILTER_STEM, "");
        if(modid.equals("all")) return null;
        return modid;
    }
}
