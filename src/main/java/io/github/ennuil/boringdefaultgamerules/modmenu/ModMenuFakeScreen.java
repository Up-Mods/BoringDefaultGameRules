package io.github.ennuil.boringdefaultgamerules.modmenu;

import io.github.ennuil.boringdefaultgamerules.config.BoringDefaultGameRulesConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.TranslatableText;

// Mod Menu doesn't like having logic in the screen factory, so, we do the ARRFAB gambit of having a fake screen!
public class ModMenuFakeScreen extends Screen {
    private final Screen parentScreen;

    public ModMenuFakeScreen(Screen screen) {
        super(null);
        parentScreen = screen;
    }
    
    @Override
    protected void init() {
        BoringDefaultGameRulesConfig.loadOrCreateConfig();
        this.client.getToastManager().add(SystemToast.create(
            this.client,
            SystemToast.Type.TUTORIAL_HINT,
            new TranslatableText("toast.boringdefaultgamerules.title"),
            new TranslatableText("toast.boringdefaultgamerules.reload")
        ));
        this.client.setScreen(parentScreen);
    }

    @Override
    protected void narrateScreenIfNarrationEnabled(boolean useTranslationsCache) {}
}
