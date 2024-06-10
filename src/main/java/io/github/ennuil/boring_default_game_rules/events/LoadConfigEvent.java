package io.github.ennuil.boring_default_game_rules.events;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.base.api.entrypoint.server.DedicatedServerModInitializer;

import io.github.ennuil.boring_default_game_rules.config.ModConfigManager;

public class LoadConfigEvent implements ClientModInitializer, DedicatedServerModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		ModConfigManager.init(true);
	}

	@Override
	public void onInitializeServer(ModContainer mod) {
		ModConfigManager.init(false);
	}
}
