package io.github.ennuil.boring_default_game_rules.config;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.ennuil.boring_default_game_rules.mixin.BoundedIntRuleAccessor;
import io.github.ennuil.boring_default_game_rules.mixin.DoubleRuleAccessor;
import io.github.ennuil.boring_default_game_rules.mixin.EnumRuleAccessor;
import io.github.ennuil.boring_default_game_rules.utils.LoggingUtils;
import io.github.ennuil.boring_default_game_rules.wrench_wrapper.WrenchWrapper;
import net.fabricmc.fabric.api.gamerule.v1.FabricGameRuleVisitor;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.fabricmc.fabric.impl.gamerule.rule.BoundedIntRule;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.feature_flags.FeatureFlagBitSet;
import net.minecraft.feature_flags.FeatureFlags;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Language;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class ModConfigManager {
	public static final String SCHEMA_FILE_NAME = "config.schema.json";
	public static final Path SCHEMA_DIRECTORY_PATH = WrenchWrapper.getConfigDir().resolve("boring_default_game_rules");
	public static final Path SCHEMA_PATH = SCHEMA_DIRECTORY_PATH.resolve(SCHEMA_FILE_NAME);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static final ModConfig CONFIG = WrenchWrapper.create("boring_default_game_rules", "config", ModConfig.class);

	private static JsonObject defaultGameRulesProperties;
	private static String newSchemaHash = "";

	private static boolean active = false;

	private ModConfigManager() {}

	public static void init(boolean client) {
		ModConfigManager.generateGameRulesHash();
		ModConfigManager.prepareSchema(client);
		CONFIG.save();

		ModConfigManager.active = true;
	}

	public static boolean isActive() {
		return ModConfigManager.active;
	}

	public static void prepareSchema(boolean client) {
		try {
			boolean generateNewSchema = false;

			if (CONFIG.generateJsonSchema.value()) {
				if (SCHEMA_PATH.toFile().exists()) {
					Reader schemaReader = Files.newBufferedReader(SCHEMA_PATH, StandardCharsets.UTF_8);
					JsonObject schemaJson = JsonHelper.deserialize(GSON, schemaReader, JsonObject.class, true);
					String schemaHash = schemaJson.get("gameRulesHash").getAsString();
					schemaReader.close();

					if (!schemaHash.equals(newSchemaHash)) {
						LoggingUtils.LOGGER.info("The loaded set of game rules doesn't match the current schema's ones! This schema will be regenerated.");
						generateNewSchema = true;
					}
				} else {
					generateNewSchema = true;
				}
			}

			if (generateNewSchema) {
				if (!Files.isDirectory(SCHEMA_DIRECTORY_PATH)) {
					LoggingUtils.LOGGER.info("A folder for saving the schema hasn't been found! Creating one...");
					Files.createDirectory(SCHEMA_DIRECTORY_PATH);
				}

				LoggingUtils.LOGGER.info("Generating a new JSON schema...");
				if (client) {
					generateGameRulePropertiesOnClient();
				} else {
					generateGameRulePropertiesOnServer();
				}
				Writer schemaWriter = Files.newBufferedWriter(SCHEMA_PATH, StandardCharsets.UTF_8);
				GSON.toJson(createSchemaObject(newSchemaHash), schemaWriter);
				schemaWriter.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void resetDefaults() {
		updateConfig(null);
	}

	public static void updateConfig(GameRules newGameRules) {
		CONFIG.defaultGameRules.value().clear();
		var defaultGameRules = new GameRules(FeatureFlags.MAIN_REGISTRY.setOf());

		if (newGameRules != null) {
			defaultGameRules.accept(new FabricGameRuleVisitor() {
				@Override
				public void visitBooleanGameRule(GameRules.Key<GameRules.BooleanGameRule> key, GameRules.Type<GameRules.BooleanGameRule> type) {
					if (newGameRules.get(key).getValue() != defaultGameRules.get(key).getValue()) {
						CONFIG.defaultGameRules.value().put(key.getName(), newGameRules.get(key).getValue());
					}
				}

				@Override
				public void visitIntGameRule(GameRules.Key<GameRules.IntGameRule> key, GameRules.Type<GameRules.IntGameRule> type) {
					if (newGameRules.get(key).getValue() != defaultGameRules.get(key).getValue()) {
						CONFIG.defaultGameRules.value().put(key.getName(), newGameRules.get(key).getValue());
					}
				}

				@Override
				public void visitDouble(GameRules.Key<DoubleRule> key, GameRules.Type<DoubleRule> type) {
					if (newGameRules.get(key).get() != defaultGameRules.get(key).get()) {
						CONFIG.defaultGameRules.value().put(key.getName(), newGameRules.get(key).get());
					}
				}

				@Override
				public <E extends Enum<E>> void visitEnum(GameRules.Key<EnumRule<E>> key, GameRules.Type<EnumRule<E>> type) {
					if (!newGameRules.get(key).get().equals(defaultGameRules.get(key).get())) {
						CONFIG.defaultGameRules.value().put(key.getName(), newGameRules.get(key).get().name());
					}
				}
			});
		}

		CONFIG.save();
	}

	private static void generateGameRulePropertiesOnClient() {
		var allGameRules = new GameRules(FeatureFlags.MAIN_REGISTRY.setOf());
		defaultGameRulesProperties = new JsonObject();

		allGameRules.accept(new FabricGameRuleVisitor() {
			@Override
			public void visitBooleanGameRule(GameRules.Key<GameRules.BooleanGameRule> key, GameRules.Type<GameRules.BooleanGameRule> type) {
				addBooleanGameRule(
					key.getName(),
					I18n.translate(key.getTranslationKey()),
					I18n.hasTranslation(key.getTranslationKey() + ".description")
						? Text.translatable(key.getTranslationKey() + ".description").getString()
						: null,
					type.createGameRule().getValue());
			}

			@Override
			public void visitIntGameRule(GameRules.Key<GameRules.IntGameRule> key, GameRules.Type<GameRules.IntGameRule> type) {
				var gameRule = type.createGameRule();
				if (gameRule instanceof BoundedIntRule boundedType) {
					int minimum = ((BoundedIntRuleAccessor) (Object) boundedType).getMinimumValue();
					int maximum = ((BoundedIntRuleAccessor) (Object) boundedType).getMaximumValue();
					addIntegerGameRule(
						key.getName(),
						I18n.translate(key.getTranslationKey()),
						I18n.hasTranslation(key.getTranslationKey() + ".description")
							? Text.translatable(key.getTranslationKey() + ".description").getString()
							: null,
						joinFeatureFlagsOnClient(type.method_61728()),
						boundedType.getValue(),
						minimum,
						maximum);
				} else {
					addIntegerGameRule(
						key.getName(),
						I18n.translate(key.getTranslationKey()),
						I18n.hasTranslation(key.getTranslationKey() + ".description")
							? Text.translatable(key.getTranslationKey() + ".description").getString()
							: null,
						joinFeatureFlagsOnClient(type.method_61728()),
						type.createGameRule().getValue(),
						type.argumentType.get() instanceof IntegerArgumentType intArgType ? intArgType.getMinimum() : null,
						type.argumentType.get() instanceof IntegerArgumentType intArgType ? intArgType.getMaximum() : null);
				}
			}

			@Override
			public void visitDouble(GameRules.Key<DoubleRule> key, GameRules.Type<DoubleRule> type) {
				var doubleRule = type.createGameRule();
				double maximum = ((DoubleRuleAccessor) (Object) doubleRule).getMaximumValue();
				double minimum = ((DoubleRuleAccessor) (Object) doubleRule).getMinimumValue();
				addDoubleGameRule(
					key.getName(),
					I18n.translate(key.getTranslationKey()),
					I18n.hasTranslation(key.getTranslationKey() + ".description")
						? Text.translatable(key.getTranslationKey() + ".description").getString()
						: null,
					doubleRule.get(),
					minimum,
					maximum
				);
			}

			@Override
			public <E extends Enum<E>> void visitEnum(GameRules.Key<EnumRule<E>> key, GameRules.Type<EnumRule<E>> type) {
				var enumRule = type.createGameRule();
				addEnumGameRule(
					key.getName(),
					I18n.translate(key.getTranslationKey()),
					I18n.hasTranslation(key.getTranslationKey() + ".description")
						? Text.translatable(key.getTranslationKey() + ".description").getString()
						: null,
					enumRule.get(),
					((EnumRuleAccessor<E>) (Object) enumRule).getSupportedValues());
			}
		});
	}

	private static void generateGameRulePropertiesOnServer() {
		var allGameRules = new GameRules(FeatureFlags.MAIN_REGISTRY.setOf());
		defaultGameRulesProperties = new JsonObject();

		allGameRules.accept(new FabricGameRuleVisitor() {
			final Language language = Language.getInstance();

			@Override
			public void visitBooleanGameRule(GameRules.Key<GameRules.BooleanGameRule> key, GameRules.Type<GameRules.BooleanGameRule> type) {
				addBooleanGameRule(
					key.getName(),
					language.get(key.getTranslationKey()),
					language.get(key.getTranslationKey() + ".description", null),
					type.createGameRule().getValue());
			}

			@Override
			public void visitIntGameRule(GameRules.Key<GameRules.IntGameRule> key, GameRules.Type<GameRules.IntGameRule> type) {
				if (type.createGameRule() instanceof BoundedIntRule boundedType) {
					int minimum = ((BoundedIntRuleAccessor) (Object) boundedType).getMinimumValue();
					int maximum = ((BoundedIntRuleAccessor) (Object) boundedType).getMaximumValue();
					addIntegerGameRule(
						key.getName(),
						language.get(key.getTranslationKey()),
						language.get(key.getTranslationKey() + ".description", null),
						joinFeatureFlagsOnServer(type.method_61728(), language),
						boundedType.getValue(),
						minimum,
						maximum);
				} else {
					addIntegerGameRule(
						key.getName(),
						language.get(key.getTranslationKey()),
						language.get(key.getTranslationKey() + ".description", null),
						joinFeatureFlagsOnServer(type.method_61728(), language),
						type.createGameRule().getValue(),
						type.argumentType.get() instanceof IntegerArgumentType intArgType ? intArgType.getMinimum() : null,
						type.argumentType.get() instanceof IntegerArgumentType intArgType ? intArgType.getMaximum() : null);
				}
			}

			@Override
			public void visitDouble(GameRules.Key<DoubleRule> key, GameRules.Type<DoubleRule> type) {
				DoubleRule doubleRule = type.createGameRule();
				double maximum = ((DoubleRuleAccessor) (Object) doubleRule).getMaximumValue();
				double minimum = ((DoubleRuleAccessor) (Object) doubleRule).getMinimumValue();
				addDoubleGameRule(
					key.getName(),
					language.get(key.getTranslationKey()),
					language.get(key.getTranslationKey() + ".description", null),
					doubleRule.get(),
					minimum,
					maximum);
			}

			@Override
			public <E extends Enum<E>> void visitEnum(GameRules.Key<EnumRule<E>> key, GameRules.Type<EnumRule<E>> type) {
				EnumRule<E> enumRule = type.createGameRule();
				addEnumGameRule(
					key.getName(),
					language.get(key.getTranslationKey()),
					language.get(key.getTranslationKey() + ".description", null),
					enumRule.get(),
					((EnumRuleAccessor<E>) (Object) enumRule).getSupportedValues());
			}
		});
	}

	private static String joinFeatureFlagsOnClient(FeatureFlagBitSet set) {
		return FeatureFlags.MAIN_REGISTRY.getFlagIds(set).stream().map(id -> "dataPack." + id.getPath() + ".name").map(I18n::translate).collect(Collectors.joining(", "));
	}

	private static String joinFeatureFlagsOnServer(FeatureFlagBitSet set, Language language) {
		return FeatureFlags.MAIN_REGISTRY.getFlagIds(set).stream().map(id -> "dataPack." + id.getPath() + ".name").map(language::get).collect(Collectors.joining(", "));
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
		GameRules.GAME_RULE_TYPES.keySet().forEach(key -> newSchemaHash += key.getName());
		newSchemaHash = Hashing.sha256().hashString(newSchemaHash, StandardCharsets.UTF_8).toString();
	}
}
