package io.github.ennuil.boring_default_game_rules.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.fabric.impl.gamerule.rule.BoundedIntRule;

@Mixin(BoundedIntRule.class)
public interface BoundedIntRuleAccessor {
    @Accessor
    int getMinimumValue();

    @Accessor
    void setMinimumValue(int minimumValue);

    @Accessor
    int getMaximumValue();

    @Accessor
    void setMaximumValue(int maximumValue);
}
