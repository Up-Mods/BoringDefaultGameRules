package io.github.ennuil.boring_default_game_rules.mixin.client.accessors;

import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EditGameRulesScreen.class)
public interface EditGameRulesScreenAccessor {
	@Accessor
	EditGameRulesScreen.RuleList getRuleList();

	@Mutable
	@Accessor
	void setGameRules(GameRules gameRules);
}
