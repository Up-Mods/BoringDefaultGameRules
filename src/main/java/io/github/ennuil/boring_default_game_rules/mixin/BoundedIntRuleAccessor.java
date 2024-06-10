package io.github.ennuil.boring_default_game_rules.mixin;

import net.fabricmc.fabric.impl.gamerule.rule.BoundedIntRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BoundedIntRule.class)
public interface BoundedIntRuleAccessor {
    @Accessor(remap = false)
    int getMinimumValue();

    @Accessor(remap = false)
    int getMaximumValue();
}
