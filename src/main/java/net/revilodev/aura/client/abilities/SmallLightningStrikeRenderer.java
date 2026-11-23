package net.revilodev.aura.client.abilities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.revilodev.aura.CodexMod;
import net.revilodev.aura.entity.effect.SmallLightningStrikeEntity;

public final class SmallLightningStrikeRenderer extends EntityRenderer<SmallLightningStrikeEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/particle/lightning-strike.png");

    public SmallLightningStrikeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SmallLightningStrikeEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(SmallLightningStrikeEntity entity) {
        return TEXTURE;
    }
}
