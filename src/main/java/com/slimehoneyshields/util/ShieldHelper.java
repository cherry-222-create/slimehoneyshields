package com.slimehoneyshields.util;

import com.slimehoneyshields.Config;
import com.slimehoneyshields.item.HoneyBlockShieldItem;
import com.slimehoneyshields.item.SlimeBlockShieldItem;
import com.slimehoneyshields.registry.ModEnchantments;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * Stateless helpers shared by the block/projectile event handlers.
 */
public final class ShieldHelper {
    public static boolean isShield(ItemStack stack) {
        return stack.getItem() instanceof SlimeBlockShieldItem
                || stack.getItem() instanceof HoneyBlockShieldItem;
    }

    public static boolean isSlime(ItemStack stack) {
        return stack.getItem() instanceof SlimeBlockShieldItem;
    }

    public static boolean isHoney(ItemStack stack) {
        return stack.getItem() instanceof HoneyBlockShieldItem;
    }

    /** Returns the actual shield stack used to block, or {@link ItemStack#EMPTY} if none. */
    public static ItemStack getBlockingShield(LivingEntity entity) {
        if (!entity.isBlocking()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = entity.getUseItem();
        return isShield(stack) ? stack : ItemStack.EMPTY;
    }

    /**
     * Requested formula: 3 blocks minimum, 32 blocks maximum, scaling with the damage that was
     * blocked. Rebound multiplies the result, still respecting the hard cap.
     */
    public static double slimeDistance(float blockedDamage, ItemStack shield) {
        double min = Config.SLIME_MIN_DISTANCE.get();
        double max = Math.max(min, Config.SLIME_MAX_DISTANCE.get());
        double distance = min + blockedDamage * Config.SLIME_DAMAGE_SCALE.get();
        distance = Mth.clamp(distance, min, max);

        int rebound = shield.getEnchantmentLevel(ModEnchantments.REBOUND.get());
        if (rebound > 0) {
            distance *= 1.0D + rebound * Config.SLIME_REBOUND_BONUS.get();
            distance = Math.min(max, distance);
        }
        return distance;
    }

    /**
     * Sets a horizontal launch velocity that travels roughly {@code distance} blocks.
     *
     * <p>For an airborne entity Minecraft keeps ~91% of horizontal velocity per tick, so a total
     * distance of D needs an initial speed near D/7.9 once the first ground-friction tick, the
     * small upward arc and the landing slide are accounted for. The divisor is configurable for
     * packs that tune entity physics.</p>
     */
    public static void launchAway(LivingEntity attacker, LivingEntity blocker, double distance) {
        if (attacker.isRemoved() || !attacker.isAlive()) {
            return;
        }

        Vec3 direction = attacker.position().subtract(blocker.position());
        direction = new Vec3(direction.x, 0.0D, direction.z);
        if (direction.lengthSqr() < 1.0E-4D) {
            Vec3 look = blocker.getViewVector(1.0F);
            direction = new Vec3(-look.x, 0.0D, -look.z);
        }
        if (direction.lengthSqr() < 1.0E-4D) {
            return;
        }
        direction = direction.normalize();

        double speed = distance / Math.max(0.1D, Config.SLIME_LAUNCH_DIVISOR.get());
        attacker.setDeltaMovement(direction.x * speed, Config.SLIME_LAUNCH_UPWARD.get(), direction.z * speed);
        attacker.setOnGround(false);
        attacker.hasImpulse = true;
        attacker.hurtMarked = true;
        attacker.setSprinting(false);
        attacker.fallDistance = 0.0F;

        slimeImpact(blocker.level(), attacker);
    }

    public static int honeyDurationTicks(ItemStack shield) {
        int base = Config.HONEY_BASE_STUN_TICKS.get();
        int sticky = shield.getEnchantmentLevel(ModEnchantments.STICKY.get());
        return base + sticky * Config.HONEY_STICKY_BONUS_TICKS.get();
    }

    /**
     * Filters out projectiles that are not an attack (fishing hooks, firework rockets) and piercing
     * arrows, which vanilla shields also let through.
     */
    public static boolean isHandledProjectile(Projectile projectile) {
        if (projectile instanceof FishingHook || projectile instanceof FireworkRocketEntity) {
            return false;
        }
        return !(projectile instanceof AbstractArrow arrow) || arrow.getPierceLevel() <= 0;
    }

    /**
     * Mirrors {@code LivingEntity#isDamageSourceBlocked}: the shield only blocks attacks coming
     * from the hemisphere the player is looking at.
     */
    public static boolean isFacingProjectile(Player player, Projectile projectile) {
        Vec3 toPlayer = projectile.position().vectorTo(player.position());
        toPlayer = new Vec3(toPlayer.x, 0.0D, toPlayer.z);
        if (toPlayer.lengthSqr() < 1.0E-7D) {
            return false;
        }
        toPlayer = toPlayer.normalize();
        return toPlayer.dot(player.getViewVector(1.0F)) < 0.0D;
    }

    /** Reflects a projectile away from the blocker, with Rebound adding speed. */
    public static void deflectProjectile(Projectile projectile, LivingEntity blocker, ItemStack shield) {
        // Shulker bullets steer themselves every tick, so a velocity change would be overwritten.
        // Replace one with a new bullet aimed back at its shooter instead.
        if (projectile instanceof ShulkerBullet) {
            Entity owner = projectile.getOwner();
            Level level = projectile.level();
            projectile.discard();
            if (level instanceof ServerLevel serverLevel && owner instanceof LivingEntity livingOwner
                    && livingOwner != blocker && livingOwner.isAlive()) {
                ShulkerBullet reflected = new ShulkerBullet(serverLevel, blocker, livingOwner, Direction.Axis.Y);
                reflected.setPos(blocker.getX(), blocker.getEyeY(), blocker.getZ());
                serverLevel.addFreshEntity(reflected);
            }
            return;
        }

        Vec3 incoming = projectile.getDeltaMovement();
        Vec3 look = blocker.getViewVector(1.0F);

        // Treat the shield as an angled plane whose normal is the player's view. A descending
        // projectile (the classic ghast fireball) also tilts the normal upward and, as a final
        // guarantee, is never sent back downward. A horizontal arrow keeps its vertical component.
        double upwardBias = incoming.y < -0.05D ? Config.PROJECTILE_DEFLECT_UPWARD_BIAS.get() : 0.0D;
        double heightBias = Mth.clamp((projectile.getY() - blocker.getEyeY()) * 0.18D, -0.3D, 0.3D);
        Vec3 normal = new Vec3(look.x, look.y + upwardBias + heightBias, look.z).normalize();

        double dot = incoming.dot(normal);
        Vec3 reflected = dot < 0.0D
                ? incoming.subtract(normal.scale(2.0D * dot))
                : incoming.scale(-1.0D);
        if (incoming.y < -0.05D && reflected.y < 0.05D) {
            reflected = new Vec3(reflected.x, 0.35D, reflected.z);
        }
        if (reflected.lengthSqr() < 1.0E-6D) {
            reflected = new Vec3(-look.x, 0.2D, -look.z);
        }

        int rebound = shield.getEnchantmentLevel(ModEnchantments.REBOUND.get());
        double speed = Math.max(0.55D, incoming.length() * Config.PROJECTILE_BOUNCE_MULTIPLIER.get());
        speed *= 1.0D + rebound * Config.PROJECTILE_REBOUND_BONUS.get();
        speed = Math.min(speed, Config.PROJECTILE_MAX_BOUNCE_SPEED.get());

        moveOutside(projectile, blocker);
        projectile.setDeltaMovement(reflected.normalize().scale(speed));
        // Fireballs carry a separate acceleration vector; keep it aligned with the new direction
        // or the fireball would curve straight back toward the player.
        if (projectile instanceof AbstractHurtingProjectile hurtingProjectile) {
            Vec3 power = reflected.normalize().scale(0.1D);
            hurtingProjectile.xPower = power.x;
            hurtingProjectile.yPower = power.y;
            hurtingProjectile.zPower = power.z;
        }
        projectile.hasImpulse = true;
        projectile.hurtMarked = true;
        // Do not transfer ownership of an ender pearl: its owner is the entity that gets teleported
        // when it lands. A reflected pearl should fly back to its thrower, not yank the blocker.
        if (projectile.getOwner() != blocker && !(projectile instanceof ThrownEnderpearl)) {
            projectile.setOwner(blocker);
        }
    }

    /**
     * Approximates vanilla shield wear for a projectile. Arrows and tridents use the same
     * speed-to-damage estimate as their hit code; explosive projectiles cost one point;
     * snowballs, eggs and potions do not damage the shield.
     */
    public static void damageShieldForProjectile(Player player, ItemStack shield, Projectile projectile) {
        if (shield.isEmpty() || player.level().isClientSide) {
            return;
        }

        double speed = projectile.getDeltaMovement().length();
        int amount = 0;
        if (projectile instanceof ThrownTrident) {
            float damage = (float) Math.ceil(speed * 8.0D);
            amount = damage >= 3.0F ? 1 + Mth.floor(damage) : 0;
        } else if (projectile instanceof AbstractArrow) {
            float damage = (float) Math.ceil(speed * 2.0D);
            amount = damage >= 3.0F ? 1 + Mth.floor(damage) : 0;
        } else if (projectile instanceof AbstractHurtingProjectile || projectile instanceof ShulkerBullet) {
            amount = 1;
        }

        if (amount <= 0) {
            return;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.awardStat(Stats.ITEM_USED.get(shield.getItem()));
        }
        damageShield(player, shield, amount);
    }

    /**
     * Damages the shield without awarding the "item used" stat; projectile handling awards that
     * separately because vanilla's shield-damage hook is skipped for intercepted projectiles.
     */
    public static void damageShield(Player player, ItemStack shield, int amount) {
        if (amount <= 0 || shield.isEmpty() || player.level().isClientSide) {
            return;
        }
        InteractionHand hand = player.getUsedItemHand();
        shield.hurtAndBreak(amount, player, p -> {
            p.broadcastBreakEvent(hand);
            ForgeEventFactory.onPlayerDestroyItem(player, shield, hand);
            player.stopUsingItem();
        });
    }

    public static void slimeImpact(Level level, Entity entity) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ITEM_SLIME,
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                    12, 0.3D, 0.3D, 0.3D, 0.05D);
            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 0.9F, 0.8F);
        }
    }

    public static void honeyImpact(Level level, Entity entity) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FALLING_HONEY,
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                    10, 0.25D, 0.25D, 0.25D, 0.02D);
            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.BLOCKS, 0.8F, 1.1F);
        }
    }

    /**
     * Pushes a projectile just outside the blocker's hitbox so it cannot re-trigger the impact.
     * The offset is three-dimensional: a fireball arriving from above is pushed upward, not
     * sideways, which also keeps its reflected trajectory readable.
     */
    public static void moveOutside(Projectile projectile, LivingEntity blocker) {
        Vec3 center = blocker.position().add(0.0D, blocker.getBbHeight() * 0.5D, 0.0D);
        Vec3 outward = projectile.position().subtract(center);
        if (outward.lengthSqr() < 1.0E-4D) {
            Vec3 velocity = projectile.getDeltaMovement();
            outward = velocity.scale(-1.0D);
        }
        if (outward.lengthSqr() < 1.0E-4D) {
            outward = blocker.getViewVector(1.0F);
        }
        outward = outward.normalize();
        projectile.setPos(projectile.position().add(outward.scale(0.8D)));
        projectile.setOldPosAndRot();
    }

    private ShieldHelper() {
    }
}
