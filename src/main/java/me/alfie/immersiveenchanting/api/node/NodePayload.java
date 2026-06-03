package me.alfie.immersiveenchanting.api.node;

import me.alfie.alfinolib.util.ResourceId;
import net.minecraft.resources.Identifier;

/**
 * Marker interface for all node data payloads.
 */
public interface NodePayload {

    ResourceId type();
}
