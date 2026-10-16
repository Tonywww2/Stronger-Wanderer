package com.tonywww.strongerwanderer.platform;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class Platform {
    public static int minY(Level level) {
        //? if >=26.1 {
        /*return level.getMinY();
        *///?} else {
        return level.getMinBuildHeight();
        //?}
    }
    public static boolean hurt(ServerLevel level, LivingEntity target, DamageSource source, float amount) {
        //? if >=26.1 {
        /*return target.hurtServer(level, source, amount);
        *///?} else {
        return target.hurt(source, amount);
        //?}
    }
    public static void particles(ServerLevel level, ParticleOptions particle, Vec3 pos, int count, double spread) {
        int scaled = Math.min(64, count * Settings.PARTICLES.get() / 100);
        if (scaled > 0) level.sendParticles(particle, pos.x, pos.y, pos.z, scaled, spread, spread, spread, 0.02);
    }
    public static DamageSource strike(ServerLevel level, LivingEntity owner) {
        //? if >=26.1 {
        /*var id = net.minecraft.resources.Identifier.fromNamespaceAndPath("strongerwanderer", "arm_strike");
        *///?} else if >=1.21 {
        /*var id = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("strongerwanderer", "arm_strike");
        *///?} else {
        var id = new net.minecraft.resources.ResourceLocation("strongerwanderer", "arm_strike");
        //?}
        var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE, id);
        //? if >=26.1 {
        /*return new DamageSource(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getOrThrow(key), owner);
        *///?} else {
        return new DamageSource(level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(key), owner);
        //?}
    }
    private Platform() {}
}
