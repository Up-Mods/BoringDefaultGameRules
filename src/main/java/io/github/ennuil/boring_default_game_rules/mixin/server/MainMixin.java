package io.github.ennuil.boring_default_game_rules.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.datafixers.util.Pair;

import io.github.ennuil.boring_default_game_rules.config.ModConfigManager;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.pack.DataPackSettings;
import net.minecraft.server.Main;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.util.registry.DynamicRegistryManager.Frozen;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;

@Mixin(Main.class)
public class MainMixin {
    @Unique
    private static boolean bdgr$hasLoadedOnce = false;

	// Yes, this mixin has been cursed by Hashed Mojmap
    @Inject(
		method = {"method_43613", "m_cbzunkqe"},
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/level/storage/LevelStorage$Session;readLevelProperties(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/resource/pack/DataPackSettings;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/world/SaveProperties;"
        ),
		require = 1
    )
    private static void loadConfigOnServerInit(
		LevelStorage.Session session,
		OptionSet optionSet,
		OptionSpec<?> optionSpec,
		ServerPropertiesLoader loader,
		OptionSpec<?> optionSpec2,
		ResourceManager manager,
		DataPackSettings dataPackSettings,
		CallbackInfoReturnable<Pair<SaveProperties, Frozen>> cir
	) {
        if (!bdgr$hasLoadedOnce) {
            new ModConfigManager();
            bdgr$hasLoadedOnce = true;
        }
    }
}
