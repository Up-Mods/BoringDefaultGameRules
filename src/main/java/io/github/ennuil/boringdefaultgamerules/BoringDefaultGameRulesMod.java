package io.github.ennuil.boringdefaultgamerules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.ennuil.boringdefaultgamerules.config.BoringDefaultGameRulesConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class BoringDefaultGameRulesMod implements ModInitializer {
	public static final Logger modLogger = LogManager.getFormatterLogger("Boring Default Game Rules");

	@Override
	public void onInitialize() {
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			BoringDefaultGameRulesConfig.generateGameRulesHash();
			BoringDefaultGameRulesConfig.loadOrCreateConfig();
		});
	}
}
