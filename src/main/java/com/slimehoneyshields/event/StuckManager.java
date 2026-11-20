package com.slimehoneyshields.event;

import com.slimehoneyshields.registry.ModEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side authority for the honey shield's sticky state.
 *
 * <p>The sticky {@link MobEffect} provides the icon and the attribute modifiers, but it is not
 * enough on its own: some entities (notably bosses) reject mob effects, players are client-driven,
 * and custom movement can bypass the movement-speed attribute. This manager therefore also pins
 * the entity to the position where it was hit, clears its velocity and target, and cancels its
 * attacks through {@link ShieldEvents}.</p>
 */
public final class StuckManager {
    private static final Map<UUID, StuckEntity> STUCK = new HashMap<>();

    public static void stick(LivingEntity entity, int durationTicks) {
        if (entity.level().isClientSide || durationTicks <= 0) {
            return;
        }

        long now = entity.level().getGameTime();
        StuckEntity data = STUCK.get(entity.getUUID());
        if (data == null) {
            data = new StuckEntity(entity, entity.position(), entity.level().dimension(), now + durationTicks);
            STUCK.put(entity.getUUID(), data);
        } else {
            data.expiresAt = Math.max(data.expiresAt, now + durationTicks);
        }

        MobEffectInstance current = entity.getEffect(ModEffects.STUCK.get());
        if (current == null) {
            data.effectApplied = entity.addEffect(
                    new MobEffectInstance(ModEffects.STUCK.get(), durationTicks, 0, false, true, true));
        } else {
            current.update(new MobEffectInstance(ModEffects.STUCK.get(), durationTicks, 0, false, true, true));
            data.effectApplied = true;
        }

        freeze(entity, data);
    }

    public static boolean isStuck(LivingEntity entity) {
        return STUCK.containsKey(entity.getUUID());
    }

    /**
     * Called both at the start of the entity's tick (before AI) and at the end of the server tick
     * (after AI and physics), so neither AI goals nor a fast-moving player can drift away.
     */
    public static void freezeNow(LivingEntity entity) {
        if (entity.level().isClientSide) {
            return;
        }
        StuckEntity data = STUCK.get(entity.getUUID());
        if (data != null) {
            freeze(entity, data);
        }
    }

    public static void tick(ServerLevel level) {
        if (STUCK.isEmpty()) {
            return;
        }

        long now = level.getGameTime();
        Iterator<Map.Entry<UUID, StuckEntity>> iterator = STUCK.entrySet().iterator();
        while (iterator.hasNext()) {
            StuckEntity data = iterator.next().getValue();
            LivingEntity entity = data.entity;

            if (entity.level() != level) {
                continue;
            }

            // A dimension change invalidates the stored position; adopt the new one instead of
            // teleporting the entity back to coordinates in another dimension.
            if (data.dimension != entity.level().dimension()) {
                data.dimension = entity.level().dimension();
                data.position = entity.position();
            }

            boolean effectGone = data.effectApplied && !entity.hasEffect(ModEffects.STUCK.get());
            if (entity.isRemoved() || entity.isDeadOrDying() || now >= data.expiresAt || effectGone) {
                release(entity);
                iterator.remove();
            } else {
                freeze(entity, data);
            }
        }
    }

    private static void freeze(LivingEntity entity, StuckEntity data) {
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setSprinting(false);
        entity.setJumping(false);
        entity.fallDistance = 0.0F;

        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
            // Drop the target every tick: even if a targeting goal reacquires one, the melee goal
            // should not act on it before our attack event cancels the damage.
            mob.setTarget(null);
        }

        if (entity instanceof ServerPlayer player) {
            if (player.distanceToSqr(data.position) > 1.0E-4D) {
                player.teleportTo(data.position.x, data.position.y, data.position.z);
            }
        } else if (entity.position().distanceToSqr(data.position) > 1.0E-4D) {
            entity.setPos(data.position.x, data.position.y, data.position.z);
            entity.setOldPosAndRot();
        }
    }

    private static void release(LivingEntity entity) {
        if (entity.hasEffect(ModEffects.STUCK.get())) {
            entity.removeEffect(ModEffects.STUCK.get());
        }
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setSprinting(false);
        entity.fallDistance = 0.0F;
    }

    /** Called when the server stops: no persistent state is modified, so dropping the map is enough. */
    public static void clear() {
        STUCK.clear();
    }

    private static final class StuckEntity {
        private final LivingEntity entity;
        private Vec3 position;
        private ResourceKey<Level> dimension;
        private long expiresAt;
        private boolean effectApplied;

        private StuckEntity(LivingEntity entity, Vec3 position, ResourceKey<Level> dimension, long expiresAt) {
            this.entity = entity;
            this.position = position;
            this.dimension = dimension;
            this.expiresAt = expiresAt;
        }
    }

    private StuckManager() {
    }
}
