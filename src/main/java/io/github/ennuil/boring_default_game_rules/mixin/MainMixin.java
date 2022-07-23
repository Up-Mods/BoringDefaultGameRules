package io.github.ennuil.boring_default_game_rules.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.ennuil.boring_default_game_rules.config.BoringDefaultGameRulesConfig;
import net.minecraft.server.Main;

@Mixin(Main.class)
public class MainMixin {
    @Unique
    private static boolean hasLoadedOnce = false;

    @Inject(
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/level/storage/LevelStorage$Session;readLevelProperties(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/resource/DataPackSettings;)Lnet/minecraft/world/SaveProperties;"
        ),
        method = "main"
    )
    private static void mainMixin(String[] args, CallbackInfo ci) {
        if (!hasLoadedOnce) {
            BoringDefaultGameRulesConfig.generateGameRulesHash();
			BoringDefaultGameRulesConfig.loadOrCreateConfig();
            hasLoadedOnce = true;
        }
    }
}
