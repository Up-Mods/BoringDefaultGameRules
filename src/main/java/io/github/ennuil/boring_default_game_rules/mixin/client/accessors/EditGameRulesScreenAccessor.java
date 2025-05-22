package io.github.ennuil.boring_default_game_rules.mixin.client.accessors;

import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EditGameRulesScreen.class)
public interface EditGameRulesScreenAccessor {
	@Accessor
	EditGameRulesScreen.RuleList getRuleList();
}
