package com.tonywww.strongerwanderer.platform;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WardenRenderer;
//? if >=26.1.2 {
/*import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
*///?} else {
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.warden.Warden;
//?}

/** Uniform translucent material on the vanilla Warden rig, exclusive to projections. */
public final class PhantomRenderer extends WardenRenderer {
    //? if >=26.1.2 {
    /*private static final Identifier COLOR = Identifier.fromNamespaceAndPath("strongerwanderer", "textures/entity/phantom.png");
    *///?} else if >=1.21 {
    /*private static final ResourceLocation COLOR = ResourceLocation.fromNamespaceAndPath("strongerwanderer", "textures/entity/phantom.png");
    *///?} else {
    private static final ResourceLocation COLOR = new ResourceLocation("strongerwanderer", "textures/entity/phantom.png");
    //?}

    public PhantomRenderer(EntityRendererProvider.Context context) {
        super(context);
        layers.clear(); // No heartbeat, spots, tendril glow, or original textured overlays.
        shadowRadius = 0;
    }

    //? if >=26.1.2 {
    /*@Override public Identifier getTextureLocation(WardenRenderState state) { return COLOR; }
    @Override protected boolean isBodyVisible(WardenRenderState state) { return true; }
    @Override protected RenderType getRenderType(WardenRenderState state, boolean body, boolean translucent, boolean glowing) {
        return RenderTypes.entityTranslucentEmissive(COLOR);
    }
    *///?} else {
    @Override public ResourceLocation getTextureLocation(Warden entity) { return COLOR; }
    @Override protected boolean isBodyVisible(Warden entity) { return true; }
    @Override protected RenderType getRenderType(Warden entity, boolean body, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucentEmissive(COLOR);
    }
    //?}
}
