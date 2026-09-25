package net.nuclearteam.createnuclear;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.nuclearteam.createnuclear.content.radiation.capability.RadiationCapability;

/**
 * NeoForge attachment types, on Fabric's data attachment API. Upstream synced the radiation data
 * only to the player carrying it ({@code player.syncData}), hence {@code targetOnly}.
 */
@SuppressWarnings("UnstableApiUsage")
public class CNAttachmentTypes {
    public static final AttachmentType<RadiationCapability> RADIATION = AttachmentRegistry.create(
        CreateNuclear.asResource("radiation"),
        builder -> builder
            .initializer(RadiationCapability::new)
            .persistent(RadiationCapability.CODEC)
            .syncWith(RadiationCapability.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
    );

    public static void register() {
    }
}
