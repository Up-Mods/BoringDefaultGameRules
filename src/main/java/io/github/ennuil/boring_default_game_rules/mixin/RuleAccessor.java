package io.github.ennuil.boring_default_game_rules.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;

@Mixin(GameRules.Rule.class)
public interface RuleAccessor {
	@Invoker
	void callChanged(MinecraftServer server);
}
