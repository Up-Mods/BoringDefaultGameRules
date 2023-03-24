package io.github.ennuil.boring_default_game_rules.mixin;

import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(GameRules.class)
public interface GameRulesAccessor {
	@Accessor("RULE_TYPES")
	static Map<GameRules.Key<?>, GameRules.Type<?>> getRuleTypes() {
		throw new IllegalStateException("Mixin injection failed.");
	}
}
