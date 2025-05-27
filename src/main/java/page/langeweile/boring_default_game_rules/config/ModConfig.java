package page.langeweile.boring_default_game_rules.config;

import java.util.HashMap;

public record ModConfig(
	String $schema,
	HashMap<String, Object> default_game_rules,
	boolean generate_json_schema
) {}
