package io.github.ennuil.boring_default_game_rules.wrench_wrapper.quilt;

import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.implementor_api.ConfigEnvironment;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.config.v2.QuiltConfig;
import org.quiltmc.loader.impl.config.QuiltConfigImpl;

import java.nio.file.Path;

public class QuiltWrapper {
	public static <C extends ReflectiveConfig> C create(String family, String id, Class<C> configCreatorClass) {
		return QuiltConfig.create(family, id, configCreatorClass);
	}

	public static ConfigEnvironment getConfigEnvironment() {
		return QuiltConfigImpl.getConfigEnvironment();
	}

	public static Path getConfigDir() {
		return QuiltLoader.getConfigDir();
	}
}
