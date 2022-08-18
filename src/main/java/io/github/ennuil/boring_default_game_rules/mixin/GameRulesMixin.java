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

import io.github.ennuil.boring_default_game_rules.config.ModConfigManager;

@SuppressWarnings("unchecked")
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
		if (ModConfigManager.DEFAULT_GAME_RULES.value().size() == 0) return;

		ModConfigManager.DEFAULT_GAME_RULES.value();
		this.rules.forEach((key, rule) -> {
			ModConfigManager.DEFAULT_GAME_RULES.value().entrySet().forEach(entry -> {
				if (key.getName().equals(entry.getKey())) {
					if (rule instanceof IntRule intRule) {
						intRule.set((Integer) entry.getValue(), null);
					} else if (rule instanceof BooleanRule booleanRule) {
						booleanRule.set((Boolean) entry.getValue(), null);
					} else if (rule instanceof DoubleRule doubleRule) {
						((DoubleRuleAccessor)(Object) doubleRule).setValue((Double) entry.getValue());
					} else if (rule instanceof EnumRule enumRule) {
						enumRule.set(Enum.valueOf(enumRule.getEnumClass(), (String) entry.getValue()), null);
					}
				}
			});
		});
	}
}
