package io.github.ennuil.boring_default_game_rules.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Mixin(Screen.class)
public interface ScreenAccessor {
	@Accessor @Mutable
	void setTitle(Text title);
}
