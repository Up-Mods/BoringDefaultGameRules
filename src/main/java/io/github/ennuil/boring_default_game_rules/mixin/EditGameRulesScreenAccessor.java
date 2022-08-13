package io.github.ennuil.boring_default_game_rules.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screen.world.EditGameRulesScreen;

@Mixin(EditGameRulesScreen.class)
public interface EditGameRulesScreenAccessor {
	@Accessor
	public EditGameRulesScreen.RuleListWidget getRuleListWidget();
}
