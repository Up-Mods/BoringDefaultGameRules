package io.github.ennuil.boring_default_game_rules.mixin.server;

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

	// Yes, this mixin has been cursed by Hashed Mojmap
    @Inject(
		method = "m_cbzunkqe(Lnet/minecraft/world/storage/WorldSaveStorage$Session;Ljoptsimple/OptionSet;Ljoptsimple/OptionSpec;Lnet/minecraft/server/dedicated/ServerPropertiesLoader;Ljoptsimple/OptionSpec;Lnet/minecraft/server/WorldLoader$C_hkmknvtj;)Lnet/minecraft/server/WorldLoader$C_ijyqofsr;",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/server/dedicated/ServerPropertiesLoader;getPropertiesHandler()Lnet/minecraft/server/dedicated/ServerPropertiesHandler;"
        ),
		require = 1
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
            new ModConfigManager();
            bdgr$hasLoadedOnce = true;
        }
    }
}
