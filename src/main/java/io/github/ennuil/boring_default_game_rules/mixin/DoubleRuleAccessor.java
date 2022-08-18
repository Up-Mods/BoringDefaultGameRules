package io.github.ennuil.boring_default_game_rules.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;

@Mixin(DoubleRule.class)
public interface DoubleRuleAccessor {
    @Accessor(remap = false)
    double getMinimumValue();

    @Accessor(remap = false)
    void setMinimumValue(double minimumValue);

    @Accessor(remap = false)
    double getMaximumValue();

    @Accessor(remap = false)
    void setMaximumValue(double maximumValue);

    @Accessor(remap = false)
    double getValue();

    @Accessor(remap = false)
    void setValue(double value);
}
