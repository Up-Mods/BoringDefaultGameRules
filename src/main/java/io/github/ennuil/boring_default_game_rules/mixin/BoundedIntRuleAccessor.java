package io.github.ennuil.boring_default_game_rules.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.fabric.impl.gamerule.rule.BoundedIntRule;

@Mixin(BoundedIntRule.class)
public interface BoundedIntRuleAccessor {
    @Accessor(remap = false)
    int getMinimumValue();

    @Accessor(remap = false)
    void setMinimumValue(int minimumValue);

    @Accessor(remap = false)
    int getMaximumValue();

    @Accessor(remap = false)
    void setMaximumValue(int maximumValue);
}
