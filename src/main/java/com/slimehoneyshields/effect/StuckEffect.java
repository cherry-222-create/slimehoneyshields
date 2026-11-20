package com.slimehoneyshields.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * The visual/stat half of the honey shield's sticky effect: the movement and attack modifiers make
 * the entity unable to move or hit, while {@code StuckManager} does the position freeze and event
 * cancellation (so bosses that reject mob effects are still stopped).
 */
public class StuckEffect extends MobEffect {
    private static final String SPEED_MODIFIER = "8fd1f6c8-0d1a-4e6c-9a6a-0f6b6f2a4c11";
    private static final String KNOCKBACK_MODIFIER = "2c60f4de-5b1e-4a6d-b43e-6f2f0b9f7d22";
    private static final String ATTACK_MODIFIER = "b6d5a0e4-3f0f-4a8e-a1b6-2f9c7c1d8e33";

    public StuckEffect() {
        super(MobEffectCategory.HARMFUL, 0xE0A63A);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_MODIFIER, -1.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_MODIFIER, 1.0D,
                AttributeModifier.Operation.ADDITION);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, ATTACK_MODIFIER, -1.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
