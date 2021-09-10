package io.github.ennuil.boringdefaultgamerules;

import io.github.ennuil.boringdefaultgamerules.config.BoringDefaultGameRulesConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class BoringDefaultGameRulesClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            BoringDefaultGameRulesConfig.generateGameRulesHash();
			BoringDefaultGameRulesConfig.loadOrCreateConfig();
        });
    }
}
