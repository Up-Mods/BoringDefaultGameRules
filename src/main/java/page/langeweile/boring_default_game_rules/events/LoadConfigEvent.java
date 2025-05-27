package page.langeweile.boring_default_game_rules.events;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import page.langeweile.boring_default_game_rules.config.ModConfigManager;

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
