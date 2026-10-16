package com.tonywww.strongerwanderer.combat;

import com.tonywww.strongerwanderer.platform.Platform;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;

/** Vanilla particle atlas and render types: no custom shader or full-screen pass. */
public final class Effects {
    public static void charge(Warden w, boolean gravity) {
        if (!(w.level() instanceof ServerLevel l)) return;
        Platform.particles(l, gravity ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.SCULK_SOUL, w.position().add(0, 1.7, 0), 4, 0.8);
    }
    public static void impact(Warden w, Vec3 p, double radius, boolean gravity) {
        if (!(w.level() instanceof ServerLevel l)) return;
        Platform.particles(l, ParticleTypes.SONIC_BOOM, p.add(0, 1, 0), 1, 0);
        for (int i = 0; i < 16; i++) {
            double angle = i * Math.PI / 8;
            Vec3 point = p.add(Math.cos(angle) * radius, 0.15, Math.sin(angle) * radius);
            Platform.particles(l, gravity ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.SCULK_SOUL, point, 2, 0.15);
        }
        w.playSound(gravity ? SoundEvents.WARDEN_SONIC_BOOM : SoundEvents.WARDEN_ATTACK_IMPACT, 2.0f, gravity ? 0.75f : 0.85f);
    }
    public static void trail(Warden w, Vec3 from, Vec3 to) {
        if (!(w.level() instanceof ServerLevel l)) return;
        int samples = Math.min(8, Math.max(1, (int)Math.ceil(from.distanceTo(to) * 2)));
        for (int i = 0; i < samples; i++) Platform.particles(l, ParticleTypes.SCULK_SOUL, from.lerp(to, (i + 1.0) / samples).add(0, 1.2, 0), 1, 0.3);
    }
    private Effects() {}
}
