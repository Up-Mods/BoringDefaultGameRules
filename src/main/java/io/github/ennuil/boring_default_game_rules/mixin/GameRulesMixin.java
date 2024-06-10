package io.github.ennuil.boring_default_game_rules.mixin;

import io.github.ennuil.boring_default_game_rules.config.ModConfigManager;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@SuppressWarnings("unchecked")
@Mixin(GameRules.class)
public class GameRulesMixin {
	@Shadow
	@Final
	private Map<GameRules.Key<?>, GameRules.AbstractGameRule<?>> gameRules;

	@Inject(method = "<init>()V", at = @At("TAIL"))
	private <E extends Enum<E>> void overrideDefaults(CallbackInfo info) {
		ModConfigManager.validateInit();

		if (ModConfigManager.CONFIG.defaultGameRules.value().isEmpty()) return;

		this.gameRules.forEach((key, rule) -> ModConfigManager.CONFIG.defaultGameRules.value().forEach((defaultKey, defaultValue) -> {
			if (key.getName().equals(defaultKey)) {
				if (rule instanceof GameRules.IntGameRule intRule) {
					intRule.setValue(((Number) defaultValue).intValue(), null);
				} else if (rule instanceof GameRules.BooleanGameRule booleanRule) {
					booleanRule.setValue((Boolean) defaultValue, null);
				} else if (rule instanceof DoubleRule doubleRule) {
					((DoubleRuleAccessor) (Object) doubleRule).setValue(((Number) defaultValue).doubleValue());
					doubleRule.changed(null);
				} else if (rule instanceof EnumRule enumRule) {
					enumRule.set(Enum.valueOf(enumRule.getEnumClass(), (String) defaultValue), null);
				}
			}
		}));
	}
}
