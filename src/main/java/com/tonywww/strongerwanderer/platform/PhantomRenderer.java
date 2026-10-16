package com.tonywww.strongerwanderer.platform;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WardenRenderer;
//? if >=26.1.2 {
/*import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
*///?} else {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.monster.warden.Warden;
//? if forge {
import net.minecraftforge.client.model.pipeline.VertexConsumerWrapper;
//?} else {
/*import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;
*///?}
//?}

/** Vanilla Warden texture multiplied by a translucent cyan tint, exclusive to projections. */
public final class PhantomRenderer extends WardenRenderer {
    private static final int RED = 140, GREEN = 230, BLUE = 255, ALPHA = 115;
    private static final int TINT = (ALPHA << 24) | (RED << 16) | (GREEN << 8) | BLUE;

    public PhantomRenderer(EntityRendererProvider.Context context) {
        super(context);
        // The base texture retains all details and alpha cutouts. Separate opaque glow layers
        // would obscure translucency, so projections use only the tinted base model.
        layers.clear();
        shadowRadius = 0;
    }

    //? if >=26.1.2 {
    /*@Override protected boolean isBodyVisible(WardenRenderState state) { return true; }
    @Override protected int getModelTint(WardenRenderState state) { return TINT; }
    @Override protected RenderType getRenderType(WardenRenderState state, boolean body, boolean translucent, boolean glowing) {
        return RenderTypes.entityTranslucentEmissive(getTextureLocation(state));
    }
    *///?} else {
    @Override protected boolean isBodyVisible(Warden entity) { return true; }
    @Override protected RenderType getRenderType(Warden entity, boolean body, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucentEmissive(getTextureLocation(entity));
    }

    @Override public void render(Warden entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        RenderType bodyType = getRenderType(entity, true, false, false);
        MultiBufferSource tintedBuffers = type -> {
            VertexConsumer consumer = buffers.getBuffer(type);
            return type == bodyType ? new TintedConsumer(consumer) : consumer;
        };
        super.render(entity, yaw, partialTick, pose, tintedBuffers, light);
    }

    private static final class TintedConsumer extends VertexConsumerWrapper {
        private TintedConsumer(VertexConsumer parent) { super(parent); }
        //? if forge {
        @Override public VertexConsumer color(int r, int g, int b, int a) {
            parent.color(r * RED / 255, g * GREEN / 255, b * BLUE / 255, a * ALPHA / 255);
            return this;
        }
        @Override public void defaultColor(int r, int g, int b, int a) {
            parent.defaultColor(r * RED / 255, g * GREEN / 255, b * BLUE / 255, a * ALPHA / 255);
        }
        //?} else {
        /*@Override public VertexConsumer setColor(int r, int g, int b, int a) {
            parent.setColor(r * RED / 255, g * GREEN / 255, b * BLUE / 255, a * ALPHA / 255);
            return this;
        }
        *///?}
    }
    //?}
}
