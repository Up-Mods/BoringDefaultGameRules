package io.github.ennuil.boring_default_game_rules.mixin.server;

import io.github.ennuil.boring_default_game_rules.utils.LoggingUtils;
import net.minecraft.server.WorldLoader;
import net.minecraft.world.storage.WorldSaveStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.ennuil.boring_default_game_rules.config.ModConfigManager;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.server.Main;
import net.minecraft.server.dedicated.ServerPropertiesLoader;

@Mixin(Main.class)
public class MainMixin {
    @Unique
    private static boolean bdgr$hasLoadedOnce = false;

    @Inject(
		method = "method_43613",
        at = @At(value = "NEW", target = "net/minecraft/world/WorldInfo")
    )
    private static void loadConfigOnServerInit(
			WorldSaveStorage.Session session,
			OptionSet optionSet,
			OptionSpec<?> optionSpec,
			ServerPropertiesLoader serverPropertiesLoader,
			OptionSpec<?> optionSpec2,
			WorldLoader.C_hkmknvtj c_hkmknvtj,
			CallbackInfoReturnable<WorldLoader.C_ijyqofsr<?>> cir
	) {
        if (!bdgr$hasLoadedOnce) {
			LoggingUtils.LOGGER.info("b");
            new ModConfigManager();
            bdgr$hasLoadedOnce = true;
        }
    }
}
