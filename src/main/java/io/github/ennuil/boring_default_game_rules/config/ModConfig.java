package io.github.ennuil.boring_default_game_rules.config;

import org.quiltmc.config.api.Config;
import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.annotations.Processor;
import org.quiltmc.config.api.annotations.SerializedName;
import org.quiltmc.config.api.annotations.SerializedNameConvention;
import org.quiltmc.config.api.metadata.NamingSchemes;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.config.api.values.ValueMap;

@Processor("setSerializer")
@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class ModConfig extends ReflectiveConfig {
	@SerializedName("$schema")
	public final TrackedValue<String> schema = value(ModConfigManager.GENERATE_ME_MAYBE);
	public final TrackedValue<ValueMap<Object>> defaultGameRules = map((Object) "").build();
	public final TrackedValue<Boolean> generateJsonSchema = value(true);

	// TODO - Opt-in JSON5/JSONC support
	public void setSerializer(Config.Builder builder) {
		builder.format("json");
	}
}
