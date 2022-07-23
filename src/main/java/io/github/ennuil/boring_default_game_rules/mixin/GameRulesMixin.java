package io.github.ennuil.boring_default_game_rules.mixin;

import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;
import net.minecraft.world.GameRules.IntRule;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.ennuil.boring_default_game_rules.config.BoringDefaultGameRulesConfig;

@Mixin(GameRules.class)
public class GameRulesMixin {
	@Shadow
	@Final
	private Map<GameRules.Key<?>, GameRules.Rule<?>> rules;

	@Inject(
		at = @At("TAIL"),
		method = "<init>()V"
	)
	private <E extends Enum<E>> void overrideDefaults(CallbackInfo info) {
		this.rules.forEach((key, rule) -> {
			BoringDefaultGameRulesConfig.defaultGameRules.entrySet().forEach(entry -> {
				if (key.getName().equals(entry.getKey())) {
					if (rule instanceof IntRule intRule) {
						intRule.set(entry.getValue().getAsInt(), null);
					} else if (rule instanceof BooleanRule booleanRule) {
						booleanRule.set(entry.getValue().getAsBoolean(), null);
					} else if (rule instanceof DoubleRule doubleRule) {
						((DoubleRuleAccessor)(Object) doubleRule).setValue(entry.getValue().getAsDouble());
					} else if (rule instanceof EnumRule enumRule) {
						// FIXME - This entire bit of code is terrible, it has yellow squiggles
						enumRule.set(Enum.valueOf(enumRule.getEnumClass(), entry.getValue().getAsString()), null);
					}
				}
			});
		});
	}
}
