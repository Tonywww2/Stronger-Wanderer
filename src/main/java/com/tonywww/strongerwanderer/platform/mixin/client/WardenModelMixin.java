package com.tonywww.strongerwanderer.platform.mixin.client;

import com.tonywww.strongerwanderer.client.*;
import com.tonywww.strongerwanderer.combat.*;
import net.minecraft.client.model.geom.ModelPart;
//? if >=26.1 {
/*import net.minecraft.client.model.monster.warden.WardenModel;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
*///?} else {
import net.minecraft.client.model.WardenModel;
import net.minecraft.world.entity.monster.warden.Warden;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WardenModel.class)
public abstract class WardenModelMixin {
    @Shadow @Final protected ModelPart bone;
    @Shadow @Final protected ModelPart body;
    @Shadow @Final protected ModelPart head;
    @Shadow @Final protected ModelPart leftArm;
    @Shadow @Final protected ModelPart rightArm;
    @Shadow @Final protected ModelPart leftLeg;
    @Shadow @Final protected ModelPart rightLeg;
    //? if >=26.1 {
    /*@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/WardenRenderState;)V", at = @At("TAIL"))
    private void sw$animate(WardenRenderState state, CallbackInfo ci) {
        PoseSnapshot pose = (PoseSnapshot)state;
        WardenPoses.apply(pose.sw$poseSkill(), pose.sw$poseTick(), bone, body, head, leftArm, rightArm, leftLeg, rightLeg);
    }
    *///?} else {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/monster/warden/Warden;FFFFF)V", at = @At("TAIL"))
    private void sw$animate(Warden w, float limbSwing, float limbAmount, float age, float yaw, float pitch, CallbackInfo ci) {
        WardenAccess data = (WardenAccess)w;
        float tick = w.level().getGameTime() - data.sw$started() + age - w.tickCount;
        WardenPoses.apply(data.sw$skill(), tick, bone, body, head, leftArm, rightArm, leftLeg, rightLeg);
    }
    //?}
}
