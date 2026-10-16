package com.tonywww.strongerwanderer.combat;

import com.tonywww.strongerwanderer.platform.Platform;
import com.tonywww.strongerwanderer.platform.Settings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class Strikes {
    public static void sweep(Warden actor, Warden owner, Vec3 from, Vec3 to, double radius, double strength, HitLedger ledger, int segment) {
        if (!(owner.level() instanceof ServerLevel level)) return;
        radius *= Settings.RANGE.get();
        double vertical = 5.0 * Settings.RANGE.get();
        AABB area = new AABB(from, to).inflate(radius, vertical, radius);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area, target -> valid(owner, target))) {
            // Minkowski-expanded target box vs full movement segment, not only its endpoint.
            AABB box = e.getBoundingBox();
            Vec3 delta = to.subtract(from);
            double length2 = delta.horizontalDistanceSqr();
            double f = length2 < 1e-8 ? 0 : Math.max(0, Math.min(1,
                ((e.getX() - from.x) * delta.x + (e.getZ() - from.z) * delta.z) / length2));
            Vec3 point = from.lerp(to, f);
            double dx = Math.max(box.minX - point.x, Math.max(0, point.x - box.maxX));
            double dz = Math.max(box.minZ - point.z, Math.max(0, point.z - box.maxZ));
            if (dx * dx + dz * dz > radius * radius || box.maxY < point.y - vertical || box.minY > point.y + vertical) continue;
            if (!ledger.claim(segment, e.getUUID())) continue;
            float damage = (float)(owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * strength * Settings.DAMAGE.get());
            // Distinct projections are distinct hits, but respect invulnerability from other mods.
            if (Platform.hurt(level, e, Platform.strike(level, owner), damage)) {
                Vec3 away = e.position().subtract(actor.position());
                e.knockback(0.55, -away.x, -away.z);
            }
        }
    }
    public static boolean valid(Warden owner, LivingEntity target) {
        return target != owner && !(target instanceof Warden) && target.isAlive()
            && !target.isSpectator() && !owner.isAlliedTo(target)
            && (!(target instanceof Player p) || !p.isCreative());
    }
    private Strikes() {}
}
