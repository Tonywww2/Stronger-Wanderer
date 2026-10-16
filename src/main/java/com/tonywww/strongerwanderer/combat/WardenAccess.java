package com.tonywww.strongerwanderer.combat;
public interface WardenAccess {
    Skill sw$skill();
    long sw$started();
    void sw$sync(Skill skill, long started);
    WardenCombat sw$combat();
}
