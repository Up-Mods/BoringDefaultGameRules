package io.github.ennuil.boring_default_game_rules.modmenu;

import io.github.ennuil.boring_default_game_rules.config.BoringDefaultGameRulesConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.world.GameRules;

// By having a fake screen, the screen caching, which is undesirable for us, is bursted. Thanks, ARRFAB!
public class ModMenuFakeScreen extends Screen {
    private final Screen parent;

    public ModMenuFakeScreen(Screen parent) {
        super(null);
        this.parent = parent;
    }

    @Override
    protected void init() {
        BoringDefaultGameRulesConfig.loadOrCreateConfig();
        this.client.setScreen(new EditDefaultGameRulesScreen(new GameRules(), gameRulesWrapper -> {
			this.client.setScreen(this.parent);
			gameRulesWrapper.ifPresent(gameRules -> BoringDefaultGameRulesConfig.updateConfig(gameRules));
		}));
    }

    @Override
    public void narrateScreenIfNarrationEnabled(boolean useTranslationsCache) {}
}
