package io.github.ennuil.boring_default_game_rules.wrench_wrapper;

import io.github.ennuil.boring_default_game_rules.wrench_wrapper.fabric.FabricWrapper;
import io.github.ennuil.boring_default_game_rules.wrench_wrapper.quilt.QuiltWrapper;
import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.implementor_api.ConfigEnvironment;

import java.nio.file.Path;

public class WrenchWrapper {
	public static <C extends ReflectiveConfig> C create(String family, String id, Class<C> configCreatorClass) {
		if (WrenchWrapper.classExists("org.quiltmc.loader.api.QuiltLoader")) {
			return QuiltWrapper.create(family, id, configCreatorClass);
		} else if (WrenchWrapper.classExists("net.fabricmc.loader.FabricLoader")) {
			return FabricWrapper.create(family, id, configCreatorClass);
		} else {
			return null;
		}
	}

	public static ConfigEnvironment getConfigEnvironment() {
		if (WrenchWrapper.classExists("org.quiltmc.loader.api.QuiltLoader")) {
			return QuiltWrapper.getConfigEnvironment();
		} else if (WrenchWrapper.classExists("net.fabricmc.loader.FabricLoader")) {
			return FabricWrapper.getConfigEnvironment();
		} else {
			return null;
		}
	}

	public static Path getConfigDir() {
		if (WrenchWrapper.classExists("org.quiltmc.loader.api.QuiltLoader")) {
			return QuiltWrapper.getConfigDir();
		} else if (WrenchWrapper.classExists("net.fabricmc.loader.FabricLoader")) {
			return FabricWrapper.getConfigDir();
		} else {
			return null;
		}
	}

	public static boolean classExists(String className) {
		try {
			var clazz = Class.forName(className);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
