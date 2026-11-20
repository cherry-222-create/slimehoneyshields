package com.slimehoneyshields.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Holds projectiles hit by the honey shield in place for the sticky duration.
 *
 * <p>Projectiles are not living entities, so the mob effect cannot describe their state. Instead
 * their position and zero velocity are re-applied every server tick. On release arrows and thrown
 * items simply fall; explosive projectiles fizzle out so a frozen fireball cannot detonate in the
 * player's face three seconds later.</p>
 */
public final class StuckProjectileManager {
    private static final Map<UUID, StuckProjectile> STUCK = new HashMap<>();

    public static boolean isStuck(Projectile projectile) {
        return STUCK.containsKey(projectile.getUUID());
    }

    public static void stick(Projectile projectile, int durationTicks) {
        if (projectile.level().isClientSide || durationTicks <= 0 || projectile.isRemoved()
                || isStuck(projectile)) {
            return;
        }

        STUCK.put(projectile.getUUID(), new StuckProjectile(
                projectile, projectile.position(), projectile.level().getGameTime() + durationTicks));
        projectile.setDeltaMovement(Vec3.ZERO);
        projectile.hasImpulse = true;
        projectile.hurtMarked = true;
    }

    public static void tick() {
        if (STUCK.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, StuckProjectile>> iterator = STUCK.entrySet().iterator();
        while (iterator.hasNext()) {
            StuckProjectile data = iterator.next().getValue();
            Projectile projectile = data.projectile;

            if (projectile.isRemoved()) {
                iterator.remove();
                continue;
            }

            if (projectile.level().getGameTime() >= data.expiresAt) {
                release(projectile);
                iterator.remove();
                continue;
            }

            projectile.setPos(data.position.x, data.position.y, data.position.z);
            projectile.setOldPosAndRot();
            projectile.setDeltaMovement(Vec3.ZERO);
            projectile.hasImpulse = true;
        }
    }

    private static void release(Projectile projectile) {
        if (projectile.isRemoved()) {
            return;
        }

        projectile.setDeltaMovement(Vec3.ZERO);

        if (projectile instanceof AbstractHurtingProjectile || projectile instanceof ShulkerBullet) {
            if (projectile.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMOKE, projectile.getX(), projectile.getY(),
                        projectile.getZ(), 10, 0.2D, 0.2D, 0.2D, 0.02D);
            }
            projectile.discard();
        } else {
            // Arrows and thrown items are released into free fall.
            projectile.hasImpulse = true;
            projectile.hurtMarked = true;
        }
    }

    public static void clear() {
        STUCK.clear();
    }

    private static final class StuckProjectile {
        private final Projectile projectile;
        private final Vec3 position;
        private final long expiresAt;

        private StuckProjectile(Projectile projectile, Vec3 position, long expiresAt) {
            this.projectile = projectile;
            this.position = position;
            this.expiresAt = expiresAt;
        }
    }

    private StuckProjectileManager() {
    }
}
