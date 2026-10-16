//? if >=26.1 {
/*package com.tonywww.strongerwanderer.platform.mixin.client;
import com.tonywww.strongerwanderer.client.PoseSnapshot;
import com.tonywww.strongerwanderer.combat.Skill;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
@Mixin(WardenRenderState.class)
public abstract class WardenStateMixin implements PoseSnapshot {
    @Unique private Skill sw$skill = Skill.NONE;
    @Unique private float sw$tick;
    public Skill sw$poseSkill() { return sw$skill; }
    public float sw$poseTick() { return sw$tick; }
    public void sw$pose(Skill skill, float tick) { sw$skill = skill; sw$tick = tick; }
}
*///?}
