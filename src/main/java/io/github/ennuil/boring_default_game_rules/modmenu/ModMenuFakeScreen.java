package io.github.ennuil.boring_default_game_rules.modmenu;

import io.github.ennuil.boring_default_game_rules.config.ModConfigManager;
import io.github.ennuil.boring_default_game_rules.screen.EditDefaultGameRulesScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.feature_flags.FeatureFlags;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

// By having a fake screen, the screen caching, which is undesirable for us, is busted.
public class ModMenuFakeScreen extends Screen {
    private final Screen parent;

    public ModMenuFakeScreen(Screen parent) {
        super(Text.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
		ModConfigManager.prepareSchema(true);
        this.client.setScreen(new EditDefaultGameRulesScreen(new GameRules(FeatureFlags.MAIN_REGISTRY.setOf()), gameRulesWrapper -> {
			this.client.setScreen(this.parent);
			gameRulesWrapper.ifPresent(ModConfigManager::updateConfig);
		}));
    }
}
