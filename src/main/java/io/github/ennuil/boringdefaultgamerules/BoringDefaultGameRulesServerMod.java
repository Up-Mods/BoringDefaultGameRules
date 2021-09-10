package io.github.ennuil.boringdefaultgamerules;

import io.github.ennuil.boringdefaultgamerules.config.BoringDefaultGameRulesConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class BoringDefaultGameRulesServerMod implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			BoringDefaultGameRulesConfig.generateGameRulesHash();
			BoringDefaultGameRulesConfig.loadOrCreateConfig();
		});
	}
}
