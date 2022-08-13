package io.github.ennuil.boring_default_game_rules.new_config;

import java.nio.file.Path;

import org.quiltmc.config.api.WrappedConfig;
import org.quiltmc.config.api.values.ValueMap;
import org.quiltmc.loader.api.QuiltLoader;

public class ModConfig extends WrappedConfig {
	public final Path path = QuiltLoader.getConfigDir().resolve("schema").resolve("boringdefaultgamerules.schema.json");
	public final ValueMap<String> default_game_rules = ValueMap.builder("").build();
	public final boolean generate_json_schema = true;
}
