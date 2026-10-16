package com.tonywww.strongerwanderer.client;

import com.tonywww.strongerwanderer.combat.Skill;
import net.minecraft.client.model.geom.ModelPart;

/** Key poses applied to vanilla ModelParts; names, cubes, hierarchy, UVs and pivots stay vanilla. */
public final class WardenPoses {
    public static void apply(Skill skill, float tick, ModelPart bone, ModelPart body, ModelPart head,
                             ModelPart leftArm, ModelPart rightArm, ModelPart leftLeg, ModelPart rightLeg) {
        if (skill == Skill.NONE || tick < 0 || tick > skill.duration + 2) return;
        bone.getAllParts().forEach(ModelPart::resetPose);
        float windup = smooth(tick / Math.max(1, skill.launch));
        float flight = clamp((tick - skill.launch) / Math.max(1, skill.impact - skill.launch));
        // Arrive at the exact rest pose BEFORE releasing the animation to vanilla idle.
        float release = 1 - smooth((tick - skill.impact - 3) / Math.max(1, skill.duration - skill.impact - 9));
        if (skill.rush()) {
            float lean = (0.25f * windup + 0.35f * smooth(flight * 5)) * release;
            body.xRot = lean;
            head.xRot = -lean * 0.6f;
            rightArm.xRot = -(1.65f + 0.5f * flight) * windup * release;
            leftArm.xRot = -0.9f * windup * release;
            rightArm.yRot = -0.15f * release;
            leftArm.zRot = -0.25f * release;
            leftLeg.xRot = 0.6f * windup * release;
            rightLeg.xRot = -0.55f * windup * release;
            if (skill == Skill.SIDE_PHANTOMS || skill == Skill.PHANTOM_SIDE) {
                body.yRot = (0.6f - flight * 1.2f) * windup * release;
                rightArm.yRot = -0.85f * windup * release;
            }
        } else {
            float lift = tick < skill.launch ? windup : tick < skill.impact ? 1 : release;
            float slam = smooth((flight - 0.8f) * 5);
            body.xRot = (-0.18f + 0.78f * slam) * lift;
            head.xRot = -body.xRot * 0.5f;
            leftArm.xRot = rightArm.xRot = (-2.65f + 1.55f * slam) * lift;
            leftArm.zRot = -0.15f * lift; rightArm.zRot = 0.15f * lift;
            leftLeg.xRot = 0.4f * lift; rightLeg.xRot = -0.35f * lift;
            if (tick >= skill.launch && tick < skill.impact) {
                // ModelPart uses downward +Y and forward -Z: positive X pitches the head forward.
                // Use the same signed angle for the feet-pivot compensation to keep the centre fixed.
                float turn = (float)(Math.PI * 2 * smooth(flight));
                bone.xRot = turn;
                bone.y += 12 * ((float)Math.cos(turn) - 1);
                bone.z += 12 * (float)Math.sin(turn);
            }
            if (skill == Skill.GRAVITY_LEAP && tick < skill.launch) {
                leftArm.xRot = rightArm.xRot = -2.4f * windup;
            }
        }
    }
    private static float clamp(float f) { return Math.max(0, Math.min(1, f)); }
    private static float smooth(float f) { f = clamp(f); return f * f * (3 - 2 * f); }
    private WardenPoses() {}
}
