package com.slimehoneyshields.event;

import com.slimehoneyshields.SlimeHoneyShields;
import com.slimehoneyshields.registry.ModEffects;
import com.slimehoneyshields.util.ShieldHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * All gameplay hooks for the two shields.
 *
 * <p>Melee blocks are observed through Forge's {@link ShieldBlockEvent}, which only fires after the
 * game has confirmed that the shield actually blocked the hit. Projectiles are intercepted earlier,
 * in {@link ProjectileImpactEvent}, because arrows overwrite their velocity immediately after a
 * blocked hit; intercepting there lets the slime shield bounce them and the honey shield freeze
 * them without fighting vanilla's arrow code.</p>
 */
@Mod.EventBusSubscriber(modid = SlimeHoneyShields.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ShieldEvents {

    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event) {
        LivingEntity blocker = event.getEntity();
        ItemStack shield = ShieldHelper.getBlockingShield(blocker);
        if (shield.isEmpty()) {
            return;
        }

        DamageSource source = event.getDamageSource();
        Entity directEntity = source.getDirectEntity();

        if (directEntity instanceof LivingEntity attacker && attacker != blocker) {
            if (ShieldHelper.isSlime(shield)) {
                double distance = ShieldHelper.slimeDistance(event.getOriginalBlockedDamage(), shield);
                // ShieldBlockEvent runs before vanilla's blockUsingShield() knockback, which would
                // otherwise overwrite our launch velocity. Run after the entity's tick instead.
                if (blocker.level() instanceof ServerLevel serverLevel) {
                    serverLevel.getServer().execute(() -> ShieldHelper.launchAway(attacker, blocker, distance));
                }
            } else if (ShieldHelper.isHoney(shield)) {
                StuckManager.stick(attacker, ShieldHelper.honeyDurationTicks(shield));
                ShieldHelper.honeyImpact(blocker.level(), attacker);
            }
            return;
        }

        if (directEntity instanceof Projectile projectile && ShieldHelper.isHandledProjectile(projectile)) {
            // Fallback for projectiles that do not fire ProjectileImpactEvent (some modded ones).
            if (ShieldHelper.isSlime(shield)) {
                if (blocker.level() instanceof ServerLevel serverLevel) {
                    serverLevel.getServer().execute(() -> {
                        if (!projectile.isRemoved()) {
                            ShieldHelper.deflectProjectile(projectile, blocker, shield);
                        }
                    });
                }
            } else if (ShieldHelper.isHoney(shield)) {
                ShieldHelper.moveOutside(projectile, blocker);
                StuckProjectileManager.stick(projectile, ShieldHelper.honeyDurationTicks(shield));
                ShieldHelper.honeyImpact(blocker.level(), projectile);
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();

        if (StuckProjectileManager.isStuck(projectile)) {
            if (event.getRayTraceResult() instanceof EntityHitResult) {
                event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
            }
            return;
        }

        if (!(event.getRayTraceResult() instanceof EntityHitResult hitResult)) {
            return;
        }
        if (!(hitResult.getEntity() instanceof Player player) || projectile.getOwner() == player) {
            return;
        }
        if (!ShieldHelper.isHandledProjectile(projectile)) {
            return;
        }

        ItemStack shield = ShieldHelper.getBlockingShield(player);
        if (shield.isEmpty() || !ShieldHelper.isFacingProjectile(player, projectile)) {
            return;
        }

        DamageSource source = projectile.damageSources().thrown(projectile, projectile.getOwner());
        if (source.is(DamageTypeTags.BYPASSES_SHIELD)) {
            return;
        }

        // Stop vanilla from damaging the player/discarding the projectile. Doing this on both sides
        // also keeps the client's projectile from vanishing before the server's motion update.
        event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
        if (player.level().isClientSide) {
            return;
        }

        if (ShieldHelper.isSlime(shield)) {
            // Damage is computed before the velocity is reflected.
            ShieldHelper.damageShieldForProjectile(player, shield, projectile);
            ShieldHelper.deflectProjectile(projectile, player, shield);
            ShieldHelper.slimeImpact(player.level(), projectile);
        } else if (ShieldHelper.isHoney(shield)) {
            ShieldHelper.damageShieldForProjectile(player, shield, projectile);
            ShieldHelper.moveOutside(projectile, player);
            StuckProjectileManager.stick(projectile, ShieldHelper.honeyDurationTicks(shield));
            ShieldHelper.honeyImpact(player.level(), projectile);
        }
    }

    /** A stuck entity cannot deal damage, including through explosions or projectiles. */
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof LivingEntity living && living.hasEffect(ModEffects.STUCK.get())) {
            event.setCanceled(true);
        }
    }

    /** Players cannot even start a melee swing while stuck. */
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity().hasEffect(ModEffects.STUCK.get())) {
            event.setCanceled(true);
        }
    }

    /** Bows are blocked too, so a stuck player cannot keep attacking at range. */
    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        if (event.getEntity().hasEffect(ModEffects.STUCK.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        StuckManager.freezeNow(event.getEntity());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            StuckManager.tick(level);
        }
        StuckProjectileManager.tick();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        StuckManager.clear();
        StuckProjectileManager.clear();
    }

    private ShieldEvents() {
    }
}
