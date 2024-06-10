package io.github.ennuil.boring_default_game_rules.wrench_wrapper.fabric;

import net.fabricmc.loader.api.FabricLoader;
import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.serializers.TomlSerializer;
import org.quiltmc.config.implementor_api.ConfigEnvironment;
import org.quiltmc.config.implementor_api.ConfigFactory;

import java.nio.file.Path;

public class FabricWrapper {
	@SuppressWarnings("deprecation")
	private static final ConfigEnvironment CONFIG_ENVIRONMENT = new ConfigEnvironment(FabricLoader.getInstance().getConfigDir(), TomlSerializer.INSTANCE, TomlSerializer.INSTANCE);

	public static <C extends ReflectiveConfig> C create(String family, String id, Class<C> configCreatorClass) {
		return ConfigFactory.create(CONFIG_ENVIRONMENT, family, id, configCreatorClass);
	}

	public static ConfigEnvironment getConfigEnvironment() {
		return CONFIG_ENVIRONMENT;
	}

	public static Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}
}
