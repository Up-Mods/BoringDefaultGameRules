package io.github.ennuil.boring_default_game_rules.config;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.ennuil.boring_default_game_rules.mixin.BoundedIntRuleAccessor;
import io.github.ennuil.boring_default_game_rules.mixin.DoubleRuleAccessor;
import io.github.ennuil.boring_default_game_rules.mixin.EnumRuleAccessor;
import io.github.ennuil.boring_default_game_rules.utils.LoggingUtils;
import net.fabricmc.fabric.api.gamerule.v1.FabricGameRuleVisitor;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.fabricmc.fabric.impl.gamerule.rule.BoundedIntRule;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Boring Default Game Rules' GSON-based config system (with JSON Schema!)
 */
@SuppressWarnings("unchecked")
public class ModConfigManager {
	public static final String CONFIG_FILE_NAME = "config.json";
	public static final String SCHEMA_FILE_NAME = "config.schema.json";
	public static final Path CONFIG_DIRECTORY_PATH = FabricLoader.getInstance().getConfigDir().resolve("boring_default_game_rules");
	public static final Path CONFIG_PATH = CONFIG_DIRECTORY_PATH.resolve(CONFIG_FILE_NAME);
	public static final Path CONFIG_SCHEMA_PATH = CONFIG_DIRECTORY_PATH.resolve(SCHEMA_FILE_NAME);
	private static final Gson GSON = new GsonBuilder().setLenient().setPrettyPrinting().create();

	public static ModConfig config = new ModConfig(
		SCHEMA_FILE_NAME,
		new HashMap<>(),
		true
	);

	private static JsonObject defaultGameRulesProperties;
	private static String newSchemaHash = "";

	private static boolean active = false;

	private ModConfigManager() {}

	public static void init(boolean client) {
		try {
			if (Files.notExists(CONFIG_DIRECTORY_PATH)) {
				Files.createDirectory(CONFIG_DIRECTORY_PATH);
			}

			if (Files.exists(CONFIG_PATH)) {
				try (var reader = new JsonReader(Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8))) {
					config = GSON.fromJson(reader, ModConfig.class);
				}
			} else {
				updateConfigFile();
			}
		} catch (IOException e) {
			LoggingUtils.LOGGER.error("Error on initializing config!", e);
		}

		ModConfigManager.generateGameRulesHash();
		ModConfigManager.prepareSchema(client);

		ModConfigManager.active = true;
	}

	public static boolean isActive() {
		return ModConfigManager.active;
	}

	public static void prepareSchema(boolean client) {
		try {
			boolean generateNewSchema = false;

			if (config.generate_json_schema()) {
				if (CONFIG_SCHEMA_PATH.toFile().exists()) {
					try (Reader schemaReader = Files.newBufferedReader(CONFIG_SCHEMA_PATH, StandardCharsets.UTF_8)) {
						JsonObject schemaJson = GSON.fromJson(schemaReader, JsonObject.class);
						String schemaHash = schemaJson.get("gameRulesHash").getAsString();

						if (!schemaHash.equals(newSchemaHash)) {
							LoggingUtils.LOGGER.info("The loaded set of game rules doesn't match the current schema's ones! This schema will be regenerated.");
							generateNewSchema = true;
						}
					}
				} else {
					generateNewSchema = true;
				}
			}

			if (generateNewSchema) {
				if (!Files.isDirectory(CONFIG_DIRECTORY_PATH)) {
					LoggingUtils.LOGGER.info("A folder for saving the schema hasn't been found! Creating one...");
					Files.createDirectory(CONFIG_DIRECTORY_PATH);
				}

				LoggingUtils.LOGGER.info("Generating a new JSON schema...");
				if (client) {
					generateGameRulePropertiesOnClient();
				} else {
					generateGameRulePropertiesOnServer();
				}

				try (Writer schemaWriter = Files.newBufferedWriter(
					CONFIG_SCHEMA_PATH, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
				)) {
					GSON.toJson(createSchemaObject(newSchemaHash), schemaWriter);
				}
			}
		} catch (IOException e) {
			LoggingUtils.LOGGER.error("Failed to prepare the config JSON schema!", e);
		}
	}

	public static void resetDefaults() {
		updateConfig(null);
	}

	public static void updateConfig(GameRules newGameRules) {
		config.default_game_rules().clear();
		var defaultGameRules = new GameRules(FeatureFlags.REGISTRY.allFlags());

		if (newGameRules != null) {
			defaultGameRules.visitGameRuleTypes(new FabricGameRuleVisitor() {
				@Override
				public void visitBoolean(GameRules.Key<GameRules.BooleanValue> key, GameRules.Type<GameRules.BooleanValue> type) {
					if (newGameRules.getBoolean(key) != defaultGameRules.getBoolean(key)) {
						config.default_game_rules().put(key.getId(), newGameRules.getBoolean(key));
					}
				}

				@Override
				public void visitInteger(GameRules.Key<GameRules.IntegerValue> key, GameRules.Type<GameRules.IntegerValue> type) {
					if (newGameRules.getInt(key) != defaultGameRules.getInt(key)) {
						config.default_game_rules().put(key.getId(), newGameRules.getInt(key));
					}
				}

				@Override
				public void visitDouble(GameRules.Key<DoubleRule> key, GameRules.Type<DoubleRule> type) {
					if (newGameRules.getRule(key).get() != defaultGameRules.getRule(key).get()) {
						config.default_game_rules().put(key.getId(), newGameRules.getRule(key).get());
					}
				}

				@Override
				public <E extends Enum<E>> void visitEnum(GameRules.Key<EnumRule<E>> key, GameRules.Type<EnumRule<E>> type) {
					if (!newGameRules.getRule(key).get().equals(defaultGameRules.getRule(key).get())) {
						config.default_game_rules().put(key.getId(), newGameRules.getRule(key).get().name());
					}
				}
			});
		}

		updateConfigFile();
	}

	private static void updateConfigFile() {
		try (var writer = GSON.newJsonWriter(Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
			GSON.toJson(config, ModConfig.class, writer);
		} catch (IOException e) {
			LoggingUtils.LOGGER.error("Failed to update config file!", e);
		}
	}

	private static void generateGameRulePropertiesOnClient() {
		var allGameRules = new GameRules(FeatureFlags.REGISTRY.allFlags());
		defaultGameRulesProperties = new JsonObject();

		allGameRules.visitGameRuleTypes(new FabricGameRuleVisitor() {
			@Override
			public void visitBoolean(GameRules.Key<GameRules.BooleanValue> key, GameRules.Type<GameRules.BooleanValue> type) {
				addBooleanGameRule(
					key.getId(),
					I18n.get(key.getDescriptionId()),
					I18n.exists(key.getDescriptionId() + ".description")
						? Component.translatable(key.getDescriptionId() + ".description").getString()
						: null,
					type.createRule().get());
			}

			@Override
			public void visitInteger(GameRules.Key<GameRules.IntegerValue> key, GameRules.Type<GameRules.IntegerValue> type) {
				var gameRule = type.createRule();
				if (gameRule instanceof BoundedIntRule boundedType) {
					int minimum = ((BoundedIntRuleAccessor) (Object) boundedType).getMinimumValue();
					int maximum = ((BoundedIntRuleAccessor) (Object) boundedType).getMaximumValue();
					addIntegerGameRule(
						key.getId(),
						I18n.get(key.getDescriptionId()),
						I18n.exists(key.getDescriptionId() + ".description")
							? Component.translatable(key.getDescriptionId() + ".description").getString()
							: null,
						joinFeatureFlagsOnClient(type.requiredFeatures()),
						boundedType.get(),
						minimum,
						maximum);
				} else {
					addIntegerGameRule(
						key.getId(),
						I18n.get(key.getDescriptionId()),
						I18n.exists(key.getDescriptionId() + ".description")
							? Component.translatable(key.getDescriptionId() + ".description").getString()
							: null,
						joinFeatureFlagsOnClient(type.requiredFeatures()),
						type.createRule().get(),
						type.argument.get() instanceof IntegerArgumentType intArgType ? intArgType.getMinimum() : null,
						type.argument.get() instanceof IntegerArgumentType intArgType ? intArgType.getMaximum() : null);
				}
			}

			@Override
			public void visitDouble(GameRules.Key<DoubleRule> key, GameRules.Type<DoubleRule> type) {
				var doubleRule = type.createRule();
				double maximum = ((DoubleRuleAccessor) (Object) doubleRule).getMaximumValue();
				double minimum = ((DoubleRuleAccessor) (Object) doubleRule).getMinimumValue();
				addDoubleGameRule(
					key.getId(),
					I18n.get(key.getDescriptionId()),
					I18n.exists(key.getDescriptionId() + ".description")
						? Component.translatable(key.getDescriptionId() + ".description").getString()
						: null,
					doubleRule.get(),
					minimum,
					maximum
				);
			}

			@Override
			public <E extends Enum<E>> void visitEnum(GameRules.Key<EnumRule<E>> key, GameRules.Type<EnumRule<E>> type) {
				var enumRule = type.createRule();
				addEnumGameRule(
					key.getId(),
					I18n.get(key.getDescriptionId()),
					I18n.exists(key.getDescriptionId() + ".description")
						? Component.translatable(key.getDescriptionId() + ".description").getString()
						: null,
					enumRule.get(),
					((EnumRuleAccessor<E>) (Object) enumRule).getSupportedValues());
			}
		});
	}

	private static void generateGameRulePropertiesOnServer() {
		var allGameRules = new GameRules(FeatureFlags.REGISTRY.allFlags());
		defaultGameRulesProperties = new JsonObject();

		allGameRules.visitGameRuleTypes(new FabricGameRuleVisitor() {
			final Language language = Language.getInstance();

			@Override
			public void visitBoolean(GameRules.Key<GameRules.BooleanValue> key, GameRules.Type<GameRules.BooleanValue> type) {
				addBooleanGameRule(
					key.getId(),
					language.getOrDefault(key.getDescriptionId()),
					language.getOrDefault(key.getDescriptionId() + ".description", null),
					type.createRule().get());
			}

			@Override
			public void visitInteger(GameRules.Key<GameRules.IntegerValue> key, GameRules.Type<GameRules.IntegerValue> type) {
				if (type.createRule() instanceof BoundedIntRule boundedType) {
					int minimum = ((BoundedIntRuleAccessor) (Object) boundedType).getMinimumValue();
					int maximum = ((BoundedIntRuleAccessor) (Object) boundedType).getMaximumValue();
					addIntegerGameRule(
						key.getId(),
						language.getOrDefault(key.getDescriptionId()),
						language.getOrDefault(key.getDescriptionId() + ".description", null),
						joinFeatureFlagsOnServer(type.requiredFeatures(), language),
						boundedType.get(),
						minimum,
						maximum);
				} else {
					addIntegerGameRule(
						key.getId(),
						language.getOrDefault(key.getDescriptionId()),
						language.getOrDefault(key.getDescriptionId() + ".description", null),
						joinFeatureFlagsOnServer(type.requiredFeatures(), language),
						type.createRule().get(),
						type.argument.get() instanceof IntegerArgumentType intArgType ? intArgType.getMinimum() : null,
						type.argument.get() instanceof IntegerArgumentType intArgType ? intArgType.getMaximum() : null);
				}
			}

			@Override
			public void visitDouble(GameRules.Key<DoubleRule> key, GameRules.Type<DoubleRule> type) {
				DoubleRule doubleRule = type.createRule();
				double maximum = ((DoubleRuleAccessor) (Object) doubleRule).getMaximumValue();
				double minimum = ((DoubleRuleAccessor) (Object) doubleRule).getMinimumValue();
				addDoubleGameRule(
					key.getId(),
					language.getOrDefault(key.getDescriptionId()),
					language.getOrDefault(key.getDescriptionId() + ".description", null),
					doubleRule.get(),
					minimum,
					maximum);
			}

			@Override
			public <E extends Enum<E>> void visitEnum(GameRules.Key<EnumRule<E>> key, GameRules.Type<EnumRule<E>> type) {
				EnumRule<E> enumRule = type.createRule();
				addEnumGameRule(
					key.getId(),
					language.getOrDefault(key.getDescriptionId()),
					language.getOrDefault(key.getDescriptionId() + ".description", null),
					enumRule.get(),
					((EnumRuleAccessor<E>) (Object) enumRule).getSupportedValues());
			}
		});
	}

	private static String joinFeatureFlagsOnClient(FeatureFlagSet set) {
		return FeatureFlags.REGISTRY.toNames(set).stream().map(id -> "dataPack." + id.getPath() + ".name").map(I18n::get).collect(Collectors.joining(", "));
	}

	private static String joinFeatureFlagsOnServer(FeatureFlagSet set, Language language) {
		return FeatureFlags.REGISTRY.toNames(set).stream().map(id -> "dataPack." + id.getPath() + ".name").map(language::getOrDefault).collect(Collectors.joining(", "));
	}

	private static JsonObject createSchemaObject(String hashCode) {
		var schemaObject = new JsonObject();
		schemaObject.addProperty("$schema", "https://json-schema.org/draft/2020-12/schema");
		schemaObject.addProperty("title", "Boring Default Game Rules Configuration File");
		schemaObject.addProperty("gameRulesHash", hashCode);
		schemaObject.addProperty("description", "The config file for the \"Boring Default Game Rules\" mod.");
		schemaObject.addProperty("type", "object");

		var propertiesObject = new JsonObject();

		var schemaPropertyObject = new JsonObject();
		schemaPropertyObject.addProperty("type", "string");
		schemaPropertyObject.addProperty("title", "$schema");
		schemaPropertyObject.addProperty("description", "The standard method of assigning a JSON schema to a JSON file. This should either be set as \"config.schema.json\" always!");
		schemaPropertyObject.addProperty("default", SCHEMA_FILE_NAME);

		propertiesObject.add("$schema", schemaPropertyObject);

		JsonObject defaultGameRulesObject = new JsonObject();
		defaultGameRulesObject.addProperty("type", "object");
		defaultGameRulesObject.addProperty("title", "Default Game Rules");
		defaultGameRulesObject.addProperty("description", "Defines the default game rules, whose values will override the original default values. This mod provides game rule suggestions by generating a JSON schema.");
		defaultGameRulesObject.add("properties", defaultGameRulesProperties);

		propertiesObject.add("default_game_rules", defaultGameRulesObject);

		JsonObject generateJSONSchemaObject = new JsonObject();
		generateJSONSchemaObject.addProperty("type", "boolean");
		generateJSONSchemaObject.addProperty("title", "Generate JSON Schema");
		generateJSONSchemaObject.addProperty("description", "If enabled, this mod will generate a JSON schema in order to aid with configuration. You may disable this if you don't plan to change the settings and want to save space, and once disabled, you can safely remove both the schema and the \"$schema\" property.");
		generateJSONSchemaObject.addProperty("default", true);
		propertiesObject.add("generate_json_schema", generateJSONSchemaObject);

		schemaObject.add("properties", propertiesObject);

		JsonArray requiredArray = new JsonArray();
		requiredArray.add("default_game_rules");
		requiredArray.add("generate_json_schema");

		schemaObject.add("required", requiredArray);

		return schemaObject;
	}

	private static void addBooleanGameRule(String name, String visualName, @Nullable String description, boolean defaultValue) {
		JsonObject booleanGameRuleObject = new JsonObject();
		booleanGameRuleObject.addProperty("type", "boolean");
		booleanGameRuleObject.addProperty("title", visualName);
		if (description != null) {
			booleanGameRuleObject.addProperty("description", description);
		}
		booleanGameRuleObject.addProperty("default", defaultValue);

		defaultGameRulesProperties.add(name, booleanGameRuleObject);
	}

	private static void addIntegerGameRule(String name, String visualName, @Nullable String description, String set, int defaultValue, @Nullable Integer minimum, @Nullable Integer maximum) {
		JsonObject integerGameRuleObject = new JsonObject();
		integerGameRuleObject.addProperty("type", "integer");
		integerGameRuleObject.addProperty("title", visualName);
		String descriptionString = "";
		if (description != null) {
			descriptionString += description;
		}

		if (!set.isEmpty()) {
			descriptionString += ((description != null) ? " " : "") + "Exclusive on: " + set;
		}

		if (!descriptionString.isEmpty()) {
			integerGameRuleObject.addProperty("description", descriptionString);
		}

		integerGameRuleObject.addProperty("default", defaultValue);

		if (minimum != null && minimum != Integer.MIN_VALUE) {
			integerGameRuleObject.addProperty("minimum", minimum);
		}

		if (maximum != null && maximum != Integer.MAX_VALUE) {
			integerGameRuleObject.addProperty("maximum", maximum);
		}

		defaultGameRulesProperties.add(name, integerGameRuleObject);
	}

	private static void addDoubleGameRule(String name, String visualName, @Nullable String description, double defaultValue, double minimum, double maximum) {
		JsonObject doubleGameRuleObject = new JsonObject();
		doubleGameRuleObject.addProperty("type", "number");
		doubleGameRuleObject.addProperty("title", visualName);
		if (description != null) {
			doubleGameRuleObject.addProperty("description", description);
		}
		doubleGameRuleObject.addProperty("default", defaultValue);

		if (minimum != Double.MIN_NORMAL) {
			doubleGameRuleObject.addProperty("minimum", minimum);
		}

		if (maximum != Integer.MAX_VALUE) {
			doubleGameRuleObject.addProperty("maximum", maximum);
		}

		defaultGameRulesProperties.add(name, doubleGameRuleObject);
	}

	private static <E extends Enum<E>> void addEnumGameRule(String name, String visualName, @Nullable String description, E defaultValue, List<E> supportedValues) {
		JsonObject doubleGameRuleObject = new JsonObject();
		doubleGameRuleObject.addProperty("type", "string");
		doubleGameRuleObject.addProperty("title", visualName);
		if (description != null) {
			doubleGameRuleObject.addProperty("description", description);
		}
		doubleGameRuleObject.addProperty("default", defaultValue.name());

		JsonArray supportedEnumValues = new JsonArray();
		for (E supportedValue : supportedValues) {
			supportedEnumValues.add(supportedValue.name());
		}

		doubleGameRuleObject.add("enum", supportedEnumValues);

		defaultGameRulesProperties.add(name, doubleGameRuleObject);
	}

	public static void generateGameRulesHash() {
		GameRules.GAME_RULE_TYPES.keySet().forEach(key -> newSchemaHash += key.getId());
		newSchemaHash = Hashing.sha256().hashString(newSchemaHash, StandardCharsets.UTF_8).toString();
	}
}
