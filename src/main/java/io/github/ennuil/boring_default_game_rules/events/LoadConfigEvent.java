package io.github.ennuil.boring_default_game_rules.events;

import io.github.ennuil.boring_default_game_rules.config.JsonSerializer;
import io.github.ennuil.boring_default_game_rules.config.ModConfigManager;
import io.github.ennuil.boring_default_game_rules.wrench_wrapper.WrenchWrapper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;

public class LoadConfigEvent implements ModInitializer, ClientModInitializer, DedicatedServerModInitializer {
	@Override
	public void onInitialize() {
		WrenchWrapper.getConfigEnvironment().registerSerializer(JsonSerializer.INSTANCE);
	}

	@Override
	public void onInitializeClient() {
		ModConfigManager.init(true);
	}

	@Override
	public void onInitializeServer() {
		ModConfigManager.init(false);
	}
}
