package me.alfie.immersiveenchanting.mixins;

import me.alfie.immersiveenchanting.compat.ModCheck;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ApplyMixinPlugin implements IMixinConfigPlugin {
    private final static String PREFIX = ApplyMixinPlugin.class.getPackageName() + ".";

    @Override
    public void onLoad(final String mixinPackage) {  /* Nothing to do */ }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        String directory = mixinClassName.replace(PREFIX, "");
        // Remove directories which are not related to mods (client e.g., for client-related mixins)
        directory = directory.replace("client.", "");
        // If a directory is still present, it will run through the check below
        String[] elements = directory.split("\\.");

        if (elements.length == 2) {
            String modid = elements[0];
            return ModCheck.isModLoaded(modid);
        }

        return true;
    }

    @Override
    public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) { /* Nothing to do */ }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) { /* Nothing to do */ }

    @Override
    public void postApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) { /* Nothing to do */ }
}