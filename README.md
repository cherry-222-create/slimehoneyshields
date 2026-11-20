# Slime & Honey Shields

A small Forge 1.20.1 mod that adds two shields.

* **Minecraft:** 1.20.1
* **Forge:** 47.3.0
* **Mod id:** `slimehoneyshields`
* **Java:** 17
* **Dependencies:** Forge

## Items

| Item | Durability |
| --- | --- |
| `slimehoneyshields:slime_block_shield` | **504** |
| `slimehoneyshields:honey_block_shield` | **504** |

Vanilla shields have 336 durability, so both are exactly **1.5×** the vanilla value
(336 × 1.5 = 504). They are normal shields otherwise: hold right-click to block, they take
durability from blocked hits, they can be repaired (slime ball/slime block for the slime shield,
honeycomb/honey bottle/honey block for the honey shield, or planks like a vanilla shield), and
they can be enchanted at an enchanting table or anvil.

## Slime Shield

When it blo* It cannot move: position is pinned, velocity is cleared, navigation is stopped and its target is
  dropped every tick.
* It cannot attack: `AttackEntityEvent`, `LivingAttackEvent` and `ArrowLooseEvent` are cancelled for
  entities c arrying the Stuck effect, and the effect itself applies −100% movement speed and attack
  damage plus +100% knockback resistance.
* Players are teleported back to the stuck position each tick, so client movement cannot escape.
* The effect is a real registered mob effect (`slimehoneyshields:stuck`) with an amber drop icon, so
  it shows in the HUD and can be removed by milk.
* **Sticky** adds **1 second (20 ticks)** per level, up to 6 seconds at level III.cks a **melee** hit, the attacker is launched away. The launch distance is computed from
the damage that was blocked:

```
distance = clamp(3 + blockedDamage × 1.5, 3, 32) blocks
```

* Minimum: **3 blocks**
* Maximum: **32 blocks**
* A zombie hit (3 damage) is launched about 7.5 blocks; a strong hit reaches the 32-block cap.
* **Rebound** adds 20% launch distance per level, still capped at 32.

Incoming **projectiles** are reflected using the shield's **3D plane**, not a forced flat
trajectory. The shield normal is the player's view direction (tilted upward when the projectile is
descending), so the classic ghast fireball coming down from above is sent back **up** instead of
skimming horizontally. A descending projectile is never reflected into the ground by the shield.

Fireballs, throwables, arrows and tridents are all reflected this way. Piercing crossbow arrows still
pass through, matching vanilla shield rules. Shulker bullets (which steer themselves every tick) are
replaced by a new bullet aimed back at their shooter. Projectiles do **not** wall-bounce: they fly
off in the reflected direction and then follow their normal vanilla block impact.

Rebound also increases projectile reflection speed (15% per level).

## Honey Shield

When it blocks a **melee** hit, the attacker is glued in place for **3 seconds (60 ticks)**:



Incoming **projectiles** are held in place for the same duration. On release, arrows and thrown items
fall normally; explosive projectiles (fireballs, wither skulls, shulker bullets) fizzle out with smoke
instead of detonating next to the player three seconds later.

## Enchantments

| Enchantment | Max level | Applies to | Effect |
| --- | --- | --- | --- |
| `slimehoneyshields:rebound`  | III | Slime Block Shield only | +20% launch distance and +15% projectile reflection speed per level |
| `slimehoneyshields:sticky`  | III | Honey Block Shield only | +1 second sticky duration per level |

Both are non-treasure, discoverable, tradeable, max level III, and can be obtained from the
enchanting table (the shields are enchantable, value 15), from enchanted books, or via an anvil.
Each enchantment explicitly refuses to apply to the wrong shield type.

## Recipes

Slime Block Shield:

```
. S .
S H S
. S .
```

Honey Block Shield:

```
. H .
H S H
. H .
```

`S` = slime block / honey block respectively, `H` = vanilla `minecraft:shield` in both recipes.
The vanilla shield must be in the centre; the four blocks go around it. The recipe is in the
`equipment` category and the `shield` recipe group.

## Configuration

`config/slimehoneyshields-common.toml` is generated on first launch. It exposes:

* `slimeShield.minDistance` (default 3.0)
* `slimeShield.distancePerDamage` (default 1.5)
* `slimeShield.maxDistance` (default 32.0)
* `slimeShield.reboundBonusPerLevel` (default 0.20)
* `slimeShield.launchDivisor` (default 7.9; physics calibration)
* `slimeShield.launchUpward` (default 0.45)
* `slimeShield.projectileBounceMultiplier` (default 1.15)
* `slimeShield.projectileReboundBonusPerLevel` (default 0.15)
* `slimeShield.projectileMaxBounceSpeed` (default 5.0)
* `slimeShield.projectileDeflectUpwardBias` (default 0.35)
* `honeyShield.baseStunTicks` (default 60)
* `honeyShield.stickyBonusTicksPerLevel` (default 20)

Tune these if your pack changes entity physics or if you want a different balance curve.

