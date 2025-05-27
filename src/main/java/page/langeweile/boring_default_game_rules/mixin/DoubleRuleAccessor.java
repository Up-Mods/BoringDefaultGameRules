package page.langeweile.boring_default_game_rules.mixin;

import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DoubleRule.class)
public interface DoubleRuleAccessor {
    @Accessor(remap = false)
    double getMinimumValue();

    @Accessor(remap = false)
    double getMaximumValue();

    @Accessor(remap = false)
    void setValue(double value);
}
