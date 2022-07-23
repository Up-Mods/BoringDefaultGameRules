package io.github.ennuil.boring_default_game_rules.modmenu;

import io.github.ennuil.boring_default_game_rules.config.BoringDefaultGameRulesConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

// Mod Menu doesn't like having logic in the screen factory, so, we do the ARRFAB gambit of having a fake screen!
public class ModMenuFakeScreen extends Screen {
    private final Screen parent;

    public ModMenuFakeScreen(Screen parent) {
        super(null);
        this.parent = parent;
    }

    @Override
        protected void init() {
        BoringDefaultGameRulesConfig.loadOrCreateConfig();
        this.client.getToastManager().add(SystemToast.create(
            this.client,
            SystemToast.Type.TUTORIAL_HINT,
            Text.translatable("toast.boring_default_game_rules.title"),
            Text.translatable("toast.boring_default_game_rules.reload")
        ));
        this.client.setScreen(this.parent);
    }

    @Override
    public void narrateScreenIfNarrationEnabled(boolean useTranslationsCache) {}
}
