package io.github.ennuil.boring_default_game_rules.modmenu;

import io.github.ennuil.boring_default_game_rules.config.ModConfigManager;
import io.github.ennuil.boring_default_game_rules.screen.EditDefaultGameRulesScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.GameRules;

// By having a fake screen, the screen caching, which is undesirable for us, is busted.
public class ModMenuFakeScreen extends Screen {
    private final Screen parent;

    public ModMenuFakeScreen(Screen parent) {
        super(Component.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
		ModConfigManager.prepareSchema(true);
        this.minecraft.setScreen(new EditDefaultGameRulesScreen(new GameRules(FeatureFlags.REGISTRY.allFlags()), exitCallback -> {
			this.minecraft.setScreen(this.parent);
			exitCallback.ifPresent(ModConfigManager::updateConfig);
		}));
    }
}
