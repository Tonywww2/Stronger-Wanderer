package com.tonywww.strongerwanderer;

import com.tonywww.strongerwanderer.combat.*;
import com.tonywww.strongerwanderer.platform.PhantomWarden;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("strongerwanderer")
@PrefixGameTestTemplate(false)
public final class SkillGameTests {
    @GameTest(template="arena", timeoutTicks=700)
    public static void fiveSkills(GameTestHelper helper) {
        for (int x=0;x<32;x++) for(int z=0;z<32;z++) helper.setBlock(new BlockPos(x,0,z), Blocks.STONE);
        helper.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL, true);
        com.tonywww.strongerwanderer.platform.Settings.FOLLOW_CHANCE.set(0.0);
        Skill[] skills = {Skill.LION_CLAW, Skill.FOLLOWUP_CLAW, Skill.GRAVITY_LEAP, Skill.FORWARD_PHANTOMS, Skill.SIDE_PHANTOMS};
        int offset = 3;
        for (Skill skill : skills) {
            int start = offset;
            Warden[] actor = new Warden[1];
            net.minecraft.world.entity.animal.IronGolem[] targets = new net.minecraft.world.entity.animal.IronGolem[1];
            float[] health = new float[1];
            helper.runAtTickTime(start, () -> {
                Warden w = helper.spawn(EntityType.WARDEN, new BlockPos(5,1,8));
                var victim = helper.spawn(EntityType.IRON_GOLEM, new BlockPos(15,1,8));
                actor[0]=w; targets[0]=victim;
                victim.setNoAi(true);
                victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
                victim.setHealth(1000);
                health[0]=victim.getHealth();
                w.setPersistenceRequired();
                w.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, net.minecraft.util.Unit.INSTANCE, 10000);
                w.getBrain().setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, net.minecraft.util.Unit.INSTANCE, 10000);
                w.increaseAngerAt(victim,150,false);
                helper.assertTrue(((WardenAccess)w).sw$combat().start(skill,victim), "skill refused: "+skill);
            });
            helper.runAtTickTime(start+skill.impact+2, () -> {
                Warden w=actor[0];
                helper.assertTrue(targets[0].getHealth()<health[0], "no damage: "+skill+" w="+w.position()+" target="+targets[0].position());
                helper.assertTrue(w.getX()>helper.absolutePos(new BlockPos(8,1,8)).getX(), "no pursuit movement: "+skill);
            });
            helper.runAtTickTime(start+skill.duration+5, () -> {
                Warden w=actor[0];
                helper.assertTrue(((WardenAccess)w).sw$skill()==Skill.NONE,"skill failed to clear: "+skill);
                helper.assertTrue(helper.getLevel().getEntitiesOfClass(PhantomWarden.class,w.getBoundingBox().inflate(60)).isEmpty(),"projection leaked: "+skill);
                System.out.println("SW verified "+skill+" damage="+(health[0]-targets[0].getHealth())+" position="+w.position());
                w.discard();targets[0].discard();
            });
            offset += skill.duration+15;
        }
        verifyMeleeRushPriority(helper, offset);
    }

    private static void verifyMeleeRushPriority(GameTestHelper helper, int start) {
        Warden[] actor = new Warden[1];
        net.minecraft.world.entity.animal.IronGolem[] targets = new net.minecraft.world.entity.animal.IronGolem[1];
        helper.runAtTickTime(start, () -> {
            Warden w = helper.spawn(EntityType.WARDEN, new BlockPos(10,1,8));
            var victim = helper.spawn(EntityType.IRON_GOLEM, new BlockPos(13,1,8));
            actor[0] = w; targets[0] = victim;
            victim.setNoAi(true);
            victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
            victim.setHealth(1000);
            w.setPersistenceRequired();
            w.setOnGround(true);
            w.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, net.minecraft.util.Unit.INSTANCE, 10000);
            w.getBrain().setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, net.minecraft.util.Unit.INSTANCE, 10000);
            w.increaseAngerAt(victim,150,false);
            w.setAttackTarget(victim); // Vanilla target acquisition also sets the initial sonic-boom cooldown.
            // Let the regular server AI hook choose at full health, without command-forcing a skill.
        });
        helper.runAtTickTime(start+3, () -> {
            helper.assertTrue(((WardenAccess)actor[0]).sw$skill()==Skill.FORWARD_PHANTOMS,
                "full-health melee did not prefer forward rush");
            helper.assertTrue(targets[0].getHealth()==1000, "vanilla melee attacked before the ready rush");
        });
        helper.runAtTickTime(start+Skill.FORWARD_PHANTOMS.duration+18, () -> {
            helper.assertTrue(((WardenAccess)actor[0]).sw$skill()==Skill.SIDE_PHANTOMS,
                "melee did not alternate: skill="+((WardenAccess)actor[0]).sw$skill()+" pose="+actor[0].getPose()+" distance="+actor[0].distanceTo(targets[0]));
            System.out.println("SW verified full-health melee rush priority and alternation");
            actor[0].discard(); targets[0].discard();
        });
        helper.runAtTickTime(start+Skill.FORWARD_PHANTOMS.duration+22, helper::succeed);
    }
}
