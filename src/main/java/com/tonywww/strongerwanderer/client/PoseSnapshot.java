package com.tonywww.strongerwanderer.client;
import com.tonywww.strongerwanderer.combat.Skill;
public interface PoseSnapshot {
    Skill sw$poseSkill();
    float sw$poseTick();
    void sw$pose(Skill skill, float tick);
}
