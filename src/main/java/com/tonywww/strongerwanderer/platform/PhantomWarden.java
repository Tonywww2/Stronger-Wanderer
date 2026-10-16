package com.tonywww.strongerwanderer.platform;

import com.tonywww.strongerwanderer.combat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** A disposable projection: no autonomous AI, saved data, loot, collision or target acquisition. */
public final class PhantomWarden extends Warden {
    private Warden owner;
    private Vec3 origin = Vec3.ZERO, destination = Vec3.ZERO;
    private final HitLedger hits = new HitLedger();
    public PhantomWarden(EntityType<? extends Warden> type, Level level) {
        super(type, level);
        setNoAi(true); setSilent(true); setNoGravity(true); setInvulnerable(true); setInvisible(true);
        noPhysics = true; xpReward = 0;
    }
    public static void spawn(Warden owner, LivingEntity target, Skill skill, double sideways) {
        if (!(owner.level() instanceof ServerLevel level)) return;
        PhantomWarden p = new PhantomWarden(Entities.PHANTOM.get(), level);
        p.owner = owner;
        Vec3 direction = target.position().subtract(owner.position()).multiply(1, 0, 1).normalize();
        Vec3 side = new Vec3(direction.z, 0, -direction.x).scale(sideways);
        p.origin = owner.position().add(side);
        p.setPos(p.origin.x, p.origin.y, p.origin.z);
        p.destination = Terrain.landing(owner, target.position(), 24 * Settings.RANGE.get());
        Vec3 aim = p.destination.subtract(p.origin);
        float yaw = (float)Math.toDegrees(Math.atan2(-aim.x, aim.z));
        p.setYRot(yaw); p.setYBodyRot(yaw); p.setYHeadRot(yaw);
        ((WardenAccess)(Object)p).sw$sync(skill, level.getGameTime());
        level.addFreshEntity(p);
    }
    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel)) return;
        WardenAccess data = (WardenAccess)(Object)this;
        Skill skill = data.sw$skill();
        int age = (int)(level().getGameTime() - data.sw$started());
        if (owner == null || !owner.isAlive() || owner.isRemoved() || owner.level() != level() || age > skill.duration || skill == Skill.NONE) { discard(); return; }
        setDeltaMovement(Vec3.ZERO);
        if (age >= skill.launch && age <= skill.impact) {
            double f = Math.min(1.0, (age - skill.launch + 1.0) / (skill.impact - skill.launch + 1.0));
            Vec3 last = position();
            Vec3 pos = origin.lerp(destination, f).add(0, Math.sin(Math.PI * f) * skill.height, 0);
            setPos(pos.x, pos.y, pos.z);
            Effects.trail(this, last, pos);
            if (skill.rush()) Strikes.sweep(this, owner, last, pos, 1.8, 0.2, hits, 0);
        }
        if (skill == Skill.PHANTOM_LEAP && age == skill.impact) {
            Strikes.sweep(this, owner, position(), position(), 3.0, 0.2, hits, 0);
            Effects.impact(this, position(), 2.5, true);
        }
    }
    @Override public boolean isInvisibleTo(Player player) { return false; }
    @Override public boolean isPickable() { return false; }
    @Override public boolean isPushable() { return false; }
    //? if >=26.1 {
    /*@Override public boolean canBeCollidedWith(net.minecraft.world.entity.Entity other) { return false; }
    *///?} else {
    @Override public boolean canBeCollidedWith() { return false; }
    //?}
    //? if forge {
    @Override public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket() {
        return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);
    }
    //?}
}
