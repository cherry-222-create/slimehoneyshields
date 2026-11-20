package com.slimehoneyshields;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Common config. Every gameplay number the request asked for is configurable so the pack can be
 * tuned without touching code, while the defaults match the requested design exactly.
 */
public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue SLIME_MIN_DISTANCE = BUILDER
            .comment("Minimum slime-shield launch distance, in blocks.")
            .defineInRange("slimeShield.minDistance", 3.0D, 0.0D, 64.0D);

    public static final ForgeConfigSpec.DoubleValue SLIME_DAMAGE_SCALE = BUILDER
            .comment("Extra launch distance per point of blocked melee damage, in blocks.")
            .defineInRange("slimeShield.distancePerDamage", 1.5D, 0.0D, 16.0D);

    public static final ForgeConfigSpec.DoubleValue SLIME_MAX_DISTANCE = BUILDER
            .comment("Hard cap for slime-shield launch distance, in blocks.")
            .defineInRange("slimeShield.maxDistance", 128.0D, 1.0D, 128.0D);

    public static final ForgeConfigSpec.DoubleValue SLIME_REBOUND_BONUS = BUILDER
            .comment("Fraction added to the launch distance per Rebound level (0.20 = +20%).")
            .defineInRange("slimeShield.reboundBonusPerLevel", 0.20D, 0.0D, 2.0D);

    public static final ForgeConfigSpec.DoubleValue SLIME_LAUNCH_DIVISOR = BUILDER
            .comment("Calibration value: initial horizontal speed = distance / this value.",
                    "The default accounts for one tick of ground friction followed by air drag.")
            .defineInRange("slimeShield.launchDivisor", 7.9D, 1.0D, 32.0D);

    public static final ForgeConfigSpec.DoubleValue SLIME_LAUNCH_UPWARD = BUILDER
            .comment("Upward velocity given to a launched melee attacker.")
            .defineInRange("slimeShield.launchUpward", 0.45D, 0.0D, 2.0D);

    public static final ForgeConfigSpec.DoubleValue PROJECTILE_BOUNCE_MULTIPLIER = BUILDER
            .comment("Slime shield projectile reflection speed multiplier.")
            .defineInRange("slimeShield.projectileBounceMultiplier", 1.15D, 0.1D, 5.0D);

    public static final ForgeConfigSpec.DoubleValue PROJECTILE_REBOUND_BONUS = BUILDER
            .comment("Extra projectile reflection speed per Rebound level (0.15 = +15%).")
            .defineInRange("slimeShield.projectileReboundBonusPerLevel", 0.15D, 0.0D, 2.0D);

    public static final ForgeConfigSpec.DoubleValue PROJECTILE_MAX_BOUNCE_SPEED = BUILDER
            .comment("Maximum speed of a reflected projectile, to keep extreme hits sane.")
            .defineInRange("slimeShield.projectileMaxBounceSpeed", 5.0D, 0.5D, 20.0D);

    public static final ForgeConfigSpec.DoubleValue PROJECTILE_DEFLECT_UPWARD_BIAS = BUILDER
            .comment("Upward tilt added to the shield normal for descending projectiles.",
                    "This is what sends a ghast fireball coming from above back upward instead of flat.")
            .defineInRange("slimeShield.projectileDeflectUpwardBias", 0.35D, 0.0D, 1.5D);

    public static final ForgeConfigSpec.IntValue HONEY_BASE_STUN_TICKS = BUILDER
            .comment("Base honey-shield sticky duration in ticks (60 ticks = 3 seconds).")
            .defineInRange("honeyShield.baseStunTicks", 60, 1, 1200);

    public static final ForgeConfigSpec.IntValue HONEY_STICKY_BONUS_TICKS = BUILDER
            .comment("Extra sticky duration in ticks per Sticky enchantment level (20 ticks = 1 second).")
            .defineInRange("honeyShield.stickyBonusTicksPerLevel", 20, 0, 400);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private Config() {
    }
}
