package io.github.ennuil.boring_default_game_rules.events;

import io.github.ennuil.boring_default_game_rules.config.ModConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;

public class LoadConfigEvent implements ClientModInitializer, DedicatedServerModInitializer {
	@Override
	public void onInitializeClient() {
		ModConfigManager.init(true);
	}

	@Override
	public void onInitializeServer() {
		ModConfigManager.init(false);
	}
}
