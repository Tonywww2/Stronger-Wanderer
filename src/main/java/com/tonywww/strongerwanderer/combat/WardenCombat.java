package com.tonywww.strongerwanderer.combat;

import com.tonywww.strongerwanderer.platform.Settings;
import com.tonywww.strongerwanderer.platform.PhantomWarden;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;
import java.util.EnumMap;

public final class WardenCombat {
    private final Warden w;
    private final WardenAccess data;
    private final EnumMap<Skill, Long> cooldowns = new EnumMap<>(Skill.class);
    private HitLedger hits = new HitLedger();
    private LivingEntity target;
    private Vec3 origin, destination;
    private long nextAction;
    private boolean followup;
    private float facing;
    private Skill lastRush = Skill.SIDE_PHANTOMS;
    private static final Skill[] FIRST_PHASE = {Skill.LION_CLAW, Skill.FORWARD_PHANTOMS, Skill.SIDE_PHANTOMS};
    private static final Skill[] SECOND_PHASE = {Skill.LION_CLAW, Skill.GRAVITY_LEAP, Skill.FORWARD_PHANTOMS, Skill.SIDE_PHANTOMS};
    public WardenCombat(Warden w) { this.w = w; this.data = (WardenAccess)w; }

    public void tick() {
        if (!(w.level() instanceof ServerLevel level) || w instanceof PhantomWarden) return;
        if (!w.isAlive() || !Settings.ENABLED.get() || w.isNoAi()) { stop(); return; }
        long now = level.getGameTime();
        Skill skill = data.sw$skill();
        if (skill == Skill.NONE) { beforeBrain(); return; }
        int t = (int)(now - data.sw$started());
        if (t > skill.duration + 5 || target == null || !target.isAlive() || target.level() != level) { stop(); return; }
        w.getNavigation().stop();
        w.setDeltaMovement(Vec3.ZERO);
        w.fallDistance = 0;
        w.setYRot(facing); w.setYHeadRot(facing); w.setYBodyRot(facing);
        if (t < skill.launch && t % 4 == 0) Effects.charge(w, skill == Skill.GRAVITY_LEAP);

        // Each clone locks independently when it is emitted. Parent time is never its local time.
        if (skill == Skill.FORWARD_PHANTOMS && (t == 8 || t == 17 || t == 27 || t == 36)) spawn(Skill.PHANTOM_FORWARD, 0);
        if (skill == Skill.SIDE_PHANTOMS && (t == 4 || t == 6 || t == 7)) spawn(Skill.PHANTOM_SIDE, (t == 6 ? 1 : -1) * 3.5);
        if (skill == Skill.GRAVITY_LEAP && (t == 17 || t == 22 || t == 28 || t == 35)) spawn(Skill.PHANTOM_LEAP, (t % 2 == 0 ? 1 : -1) * 2.0);

        if (t == skill.launch) {
            origin = w.position();
            Vec3 toward = target.position().subtract(origin);
            double len = Math.sqrt(toward.x * toward.x + toward.z * toward.z);
            Vec3 standoff = len > 0.001 ? new Vec3(toward.x / len, 0, toward.z / len).scale(skill.rush() ? 0.5 : 0) : Vec3.ZERO;
            destination = Terrain.landing(w, target.position().subtract(standoff), (skill.rush() ? 22 : 14) * Settings.RANGE.get());
            face(destination);
        }
        if (t >= skill.launch && t <= skill.impact) {
            double progress = Math.min(1.0, (t - skill.launch + 1.0) / (skill.impact - skill.launch + 1.0));
            Vec3 desired = origin.lerp(destination, progress).add(0, Math.sin(progress * Math.PI) * skill.height, 0);
            Vec3 previous = w.position();
            // Vanilla collision sweeps keep the real Warden out of walls and terrain.
            w.move(MoverType.SELF, desired.subtract(previous));
            if (skill.rush()) {
                Strikes.sweep(w, w, previous, w.position(), 2.0, 0.9, hits, 1);
                Effects.trail(w, previous, w.position());
            }
        } else {
            // Settle naturally if a wall prevented the planned landing. Never snap to a target.
            w.move(MoverType.SELF, new Vec3(0, -0.45, 0));
        }
        if (skill == Skill.LION_CLAW && t == skill.launch - 2) Strikes.sweep(w, w, w.position(), w.position().add(w.getLookAngle().scale(2)), 2.0, 0.45, hits, 0);
        if (t == skill.impact && !skill.rush()) {
            double radius = skill == Skill.GRAVITY_LEAP ? 5.0 : 3.5;
            Strikes.sweep(w, w, w.position(), w.position(), radius, skill == Skill.GRAVITY_LEAP ? 1.2 : 1.0, hits, 1);
            Effects.impact(w, w.position(), radius, skill == Skill.GRAVITY_LEAP);
        }
        if (skill == Skill.GRAVITY_LEAP && (t == skill.impact + 21 || t == skill.impact + 33)) {
            double radius = t == skill.impact + 21 ? 5.5 : 7.0;
            Strikes.sweep(w, w, w.position(), w.position(), radius, 0.45, hits, t);
            Effects.impact(w, w.position(), radius, true);
        }
        if (skill == Skill.LION_CLAW && t == skill.impact + 4) followup = w.distanceToSqr(target) <= 144 * Settings.RANGE.get() * Settings.RANGE.get() && w.getRandom().nextDouble() < Settings.FOLLOW_CHANCE.get();
        if (skill == Skill.LION_CLAW && t >= skill.impact + 9 && followup) { start(Skill.FOLLOWUP_CLAW, target); return; }
        if (t >= skill.duration) stop();
    }
    /** Claim a ready skill before vanilla Brain can choose another walk/melee action. */
    public void beforeBrain() {
        if (!(w.level() instanceof ServerLevel level) || w instanceof PhantomWarden
            || !w.isAlive() || !Settings.ENABLED.get() || w.isNoAi() || data.sw$skill() != Skill.NONE) return;
        long now = level.getGameTime();
        if (now < nextAction || !w.onGround() || w.getPose() != Pose.STANDING) return;
        var brain = w.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.SONIC_BOOM_SOUND_DELAY)
            || brain.hasMemoryValue(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN)) return;
        LivingEntity candidate = brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (candidate == null || !Strikes.valid(w, candidate)
            || w.distanceToSqr(candidate) > Math.pow(28 * Settings.RANGE.get(), 2)) return;
        Skill chosen = select(candidate, now);
        if (chosen != Skill.NONE) start(chosen, candidate);
    }
    private boolean inMeleeRange(LivingEntity candidate) {
        double range = 6 * Settings.RANGE.get();
        return candidate != null && w.distanceToSqr(candidate) <= range * range;
    }
    private Skill select(LivingEntity target, long now) {
        if (inMeleeRange(target)) {
            Skill preferred = lastRush == Skill.FORWARD_PHANTOMS ? Skill.SIDE_PHANTOMS : Skill.FORWARD_PHANTOMS;
            Skill other = preferred == Skill.FORWARD_PHANTOMS ? Skill.SIDE_PHANTOMS : Skill.FORWARD_PHANTOMS;
            if (now >= cooldowns.getOrDefault(preferred, 0L)) return preferred;
            if (now >= cooldowns.getOrDefault(other, 0L)) return other;
        }
        boolean phaseTwo = w.getHealth() <= w.getMaxHealth() * Settings.PHASE_HEALTH.get();
        Skill[] choices = phaseTwo ? SECOND_PHASE : FIRST_PHASE;
        int first = w.getRandom().nextInt(choices.length);
        if (w.distanceToSqr(target) > 100 * Settings.RANGE.get() * Settings.RANGE.get()) first = phaseTwo ? 2 : 1;
        for (int n = 0; n < choices.length; n++) {
            Skill s = choices[(first + n) % choices.length];
            if (now >= cooldowns.getOrDefault(s, 0L)) return s;
        }
        nextAction = now + 10;
        return Skill.NONE;
    }
    public boolean start(Skill skill, LivingEntity victim) {
        if (!(w.level() instanceof ServerLevel level) || !w.isAlive() || w.getPose() != Pose.STANDING || victim == null || !Strikes.valid(w, victim) || skill == Skill.NONE || skill.projection()) return false;
        w.getBrain().stopAll(level, w);
        w.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        w.getBrain().eraseMemory(MemoryModuleType.PATH);
        w.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, victim);
        w.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200);
        w.getNavigation().stop();
        w.setDeltaMovement(Vec3.ZERO);
        target = victim; origin = destination = w.position(); hits = new HitLedger(); followup = false;
        face(victim.position());
        long now = level.getGameTime();
        long cooldown = Settings.COOLDOWN.get() * (skill.rush() ? 10L : 20L);
        cooldowns.put(skill, now + skill.duration + cooldown);
        if (skill.rush()) lastRush = skill;
        data.sw$sync(skill, now);
        w.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 1.5f, skill == Skill.GRAVITY_LEAP ? 0.65f : 1.15f);
        if (Settings.DEBUG.get()) level.players().stream().filter(p -> p.distanceToSqr(w) < 4096).forEach(p -> p.sendSystemMessage(Component.translatable("skill.strongerwanderer." + skill.name().toLowerCase(java.util.Locale.ROOT)), true));
        return true;
    }
    private void face(Vec3 point) {
        Vec3 d = point.subtract(w.position());
        if (d.horizontalDistanceSqr() > 0.001) facing = (float)Math.toDegrees(Math.atan2(-d.x, d.z));
        w.setYRot(facing); w.setYHeadRot(facing); w.setYBodyRot(facing);
    }
    private void spawn(Skill skill, double offset) { PhantomWarden.spawn(w, target, skill, offset); }
    public void stop() {
        if (data.sw$skill() == Skill.NONE) return;
        data.sw$sync(Skill.NONE, 0);
        w.setDeltaMovement(Vec3.ZERO); w.fallDistance = 0;
        long recovery = (long)(Settings.RECOVERY.get() * 20);
        if (inMeleeRange(target)) recovery = Math.min(recovery, 12);
        nextAction = w.level().getGameTime() + recovery;
        w.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        w.getBrain().eraseMemory(MemoryModuleType.PATH);
        w.getBrain().eraseMemory(MemoryModuleType.ATTACK_COOLING_DOWN);
        target = null;
    }
}
