package page.langeweile.boring_default_game_rules.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import page.langeweile.boring_default_game_rules.config.ModConfigManager;

import java.util.Map;

@SuppressWarnings("unchecked")
@Mixin(GameRules.class)
public class GameRulesMixin {
	@ModifyReturnValue(method = "method_61726", at = @At("RETURN"))
	private static GameRules.Value<?> modifyGameRules(GameRules.Value<?> original, @Local(argsOnly = true) Map.Entry<GameRules.Key<?>, GameRules.Value<?>> entry) {
		if (ModConfigManager.isActive() && !ModConfigManager.config.default_game_rules().isEmpty()) {
			var id = entry.getKey().getId();
			if (ModConfigManager.config.default_game_rules().containsKey(id)) {
				var value = ModConfigManager.config.default_game_rules().get(id);
				if (original instanceof GameRules.IntegerValue intValue) {
					intValue.set(((Number) value).intValue(), null);
				} else if (original instanceof GameRules.BooleanValue booleanValue) {
					booleanValue.set((Boolean) value, null);
				} else if (original instanceof DoubleRule doubleValue) {
					((DoubleRuleAccessor) (Object) doubleValue).setValue(((Number) value).doubleValue());
					doubleValue.onChanged(null);
				} else if (original instanceof EnumRule enumValue) {
					enumValue.set(Enum.valueOf(enumValue.getEnumClass(), (String) value), null);
				}
			}
		}


		return original;
	}

	@WrapOperation(
		method = "method_27324",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/GameRules$Type;createRule()Lnet/minecraft/world/level/GameRules$Value;"
		)
	)
	private GameRules.Value<?> modifyConditionalGameRules(GameRules.Type<?> instance, Operation<GameRules.Value<?>> original, @Local(argsOnly = true) Map.Entry<GameRules.Key<?>, GameRules.Value<?>> entry) {
		var originalValue = original.call(instance);
		if (ModConfigManager.isActive() && !ModConfigManager.config.default_game_rules().isEmpty()) {
			var id = entry.getKey().getId();
			if (ModConfigManager.config.default_game_rules().containsKey(id)) {
				var value = ModConfigManager.config.default_game_rules().get(id);
				if (originalValue instanceof GameRules.IntegerValue intValue) {
					intValue.set(((Number) value).intValue(), null);
				} else if (originalValue instanceof GameRules.BooleanValue booleanValue) {
					booleanValue.set((Boolean) value, null);
				} else if (originalValue instanceof DoubleRule doubleValue) {
					((DoubleRuleAccessor) (Object) doubleValue).setValue(((Number) value).doubleValue());
					doubleValue.onChanged(null);
				} else if (originalValue instanceof EnumRule enumValue) {
					enumValue.set(Enum.valueOf(enumValue.getEnumClass(), (String) value), null);
				}
			}
		}

		return originalValue;
	}
}
