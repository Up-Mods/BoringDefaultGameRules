package io.github.ennuil.boring_default_game_rules.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screen.world.EditGameRulesScreen;
import net.minecraft.world.GameRules;

@Mixin(EditGameRulesScreen.class)
public interface EditGameRulesScreenAccessor {
	@Accessor
	public EditGameRulesScreen.RuleListWidget getRuleListWidget();

	@Accessor
	public void setGameRules(GameRules gameRules);
}
