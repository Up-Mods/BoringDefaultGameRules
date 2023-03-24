package io.github.ennuil.boring_default_game_rules.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;

@Mixin(EnumRule.class)
public interface EnumRuleAccessor<E extends Enum<E>> {
    @Accessor(remap = false)
    List<E> getSupportedValues();
}
