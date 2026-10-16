package com.tonywww.strongerwanderer.combat;

import com.tonywww.strongerwanderer.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;

/** Searches only loaded columns and validates the complete, unchanged Warden hitbox. */
public final class Terrain {
    public static Vec3 landing(Warden w, Vec3 wanted, double maxRange) {
        Vec3 delta = wanted.subtract(w.position());
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (horizontal > maxRange) wanted = w.position().add(delta.scale(maxRange / horizontal));
        Vec3 floor = floor(w, wanted, 8);
        if (floor != null) return floor;
        // Pull back in bounded steps; never force-load or move to an unsafe column.
        for (int i = 1; i <= 8; i++) {
            floor = floor(w, wanted.lerp(w.position(), i / 8.0), 8);
            if (floor != null) return floor;
        }
        return w.position();
    }
    public static Vec3 floor(Warden w, Vec3 near, int search) {
        var level = w.level();
        for (int d = 0; d <= search; d++) {
            for (int sign : new int[]{1, -1}) {
                if (d == 0 && sign == -1) continue;
                BlockPos p = BlockPos.containing(near.x, Math.floor(near.y) - 1 + d * sign, near.z);
                if (p.getY() < Platform.minY(level) || !level.hasChunkAt(p) || !level.getWorldBorder().isWithinBounds(p)) continue;
                var shape = level.getBlockState(p).getCollisionShape(level, p);
                if (shape.isEmpty() || !level.getFluidState(p.above()).isEmpty()) continue;
                double y = p.getY() + shape.max(net.minecraft.core.Direction.Axis.Y);
                Vec3 point = new Vec3(near.x, y, near.z);
                if (level.noCollision(w, w.getBoundingBox().move(point.subtract(w.position())).deflate(0.02))) return point;
            }
        }
        return null;
    }
    private Terrain() {}
}
