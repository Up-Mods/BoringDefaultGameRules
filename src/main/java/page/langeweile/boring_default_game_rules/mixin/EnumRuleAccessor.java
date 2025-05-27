package page.langeweile.boring_default_game_rules.mixin;

import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(EnumRule.class)
public interface EnumRuleAccessor<E extends Enum<E>> {
    @Accessor(remap = false)
    List<E> getSupportedValues();
}
