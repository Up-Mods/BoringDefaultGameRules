package io.github.ennuil.boring_default_game_rules.events;

import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;

import io.github.ennuil.boring_default_game_rules.config.BoringDefaultGameRulesConfig;
import net.minecraft.client.MinecraftClient;

public class LoadConfigEvent implements ClientLifecycleEvents.Ready {
	@Override
	public void readyClient(MinecraftClient client) {
		BoringDefaultGameRulesConfig.generateGameRulesHash();
		BoringDefaultGameRulesConfig.loadOrCreateConfig();
	}
}
