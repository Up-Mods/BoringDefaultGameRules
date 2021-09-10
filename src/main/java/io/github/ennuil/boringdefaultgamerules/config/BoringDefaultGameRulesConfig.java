package io.github.ennuil.boringdefaultgamerules.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.ennuil.boringdefaultgamerules.BoringDefaultGameRulesMod;
import io.github.ennuil.boringdefaultgamerules.mixin.BoundedIntRuleAccessor;
import io.github.ennuil.boringdefaultgamerules.mixin.DoubleRuleAccessor;
import io.github.ennuil.boringdefaultgamerules.mixin.EnumRuleAccessor;
import net.fabricmc.fabric.api.gamerule.v1.FabricGameRuleVisitor;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.fabricmc.fabric.impl.gamerule.rule.BoundedIntRule;
import net.fabricmc.fabric.mixin.gamerule.GameRulesAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Language;
import net.minecraft.world.GameRules;

public class BoringDefaultGameRulesConfig {
    public static Path SCHEMA_DIRECTORY_PATH = FabricLoader.getInstance().getConfigDir().resolve("schema");
    public static Path SCHEMA_PATH = SCHEMA_DIRECTORY_PATH.resolve("boringdefaultgamerules.schema.json");
    public static Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("boringdefaultgamerules.json");

    private static JsonObject defaultGameRulesProperties;
    private static String newSchemaHash = "";
    public static JsonObject defaultGameRules = new JsonObject();

    public static void loadOrCreateConfig() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            boolean generateNewSchema = false;
            
            if (SCHEMA_PATH.toFile().exists()) {
                Reader schemaReader = Files.newBufferedReader(SCHEMA_PATH, StandardCharsets.UTF_8);
                JsonObject schemaJson = JsonHelper.deserialize(gson, schemaReader, JsonObject.class, true);
                String schemaHash = schemaJson.get("gameRulesHash").getAsString();
                schemaReader.close();

                if (!schemaHash.equals(newSchemaHash)) {
                    BoringDefaultGameRulesMod.modLogger.info("The loaded set of game rules doesn't match the current schema's ones! This schema will be regenerated unless the config says to not generate schemas.");
                    generateNewSchema = true;
                }
            } else {
                generateNewSchema = true;
            }

            if (CONFIG_PATH.toFile().exists()) {
                Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8);
                JsonObject json = JsonHelper.deserialize(gson, reader, JsonObject.class, true);
                defaultGameRules = json.get("default_game_rules").getAsJsonObject();
                generateNewSchema = json.get("generate_json_schema").getAsBoolean() ? generateNewSchema : false;
                reader.close();
            } else {
                JsonObject configObject = new JsonObject();
                configObject.addProperty("$schema", SCHEMA_PATH.toUri().toString());
                configObject.add("default_game_rules", new JsonObject());
                configObject.addProperty("generate_json_schema", true);
                Writer configWriter = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8);
                gson.toJson(configObject, configWriter);
                configWriter.close();
            }

            if (generateNewSchema) {
                if (!Files.isDirectory(SCHEMA_DIRECTORY_PATH)) {
                    BoringDefaultGameRulesMod.modLogger.info("A schema folder hasn't been found! Creating one...");
                    Files.createDirectory(SCHEMA_DIRECTORY_PATH);
                }
                
                BoringDefaultGameRulesMod.modLogger.info("Generating a new JSON schema...");
                generateGameRuleProperties();
                Writer schemaWriter = Files.newBufferedWriter(SCHEMA_PATH, StandardCharsets.UTF_8);
                gson.toJson(createSchemaObject(newSchemaHash), schemaWriter);
                schemaWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateGameRuleProperties() {
        defaultGameRulesProperties = new JsonObject();
        GameRules.accept(new FabricGameRuleVisitor() {
            Language language = Language.getInstance();

            @Override
            public void visitBoolean(GameRules.Key<GameRules.BooleanRule> key, GameRules.Type<GameRules.BooleanRule> type) {
                addBooleanGameRule(key.getName(), language.get(key.getTranslationKey()), type.createRule().get());
            }

            @Override
            public void visitInt(GameRules.Key<GameRules.IntRule> key, GameRules.Type<GameRules.IntRule> type) {
                if (type.createRule() instanceof BoundedIntRule boundedType) {
                    int minimum = ((BoundedIntRuleAccessor)(Object) boundedType).getMinimumValue();
                    int maximum = ((BoundedIntRuleAccessor)(Object) boundedType).getMaximumValue();
                    addIntegerGameRule(key.getName(), language.get(key.getTranslationKey()), boundedType.get(), Optional.of(minimum), Optional.of(maximum));
                } else {
                    addIntegerGameRule(key.getName(), language.get(key.getTranslationKey()), type.createRule().get(), Optional.empty(), Optional.empty());
                }
            }

            @Override
            public void visitDouble(GameRules.Key<DoubleRule> key, GameRules.Type<DoubleRule> type) {
                DoubleRule doubleRule = type.createRule();
                double maximum = ((DoubleRuleAccessor)(Object) doubleRule).getMaximumValue();
                double minimum = ((DoubleRuleAccessor)(Object) doubleRule).getMinimumValue();
                addDoubleGameRule(key.getName(), language.get(key.getTranslationKey()), doubleRule.get(), minimum, maximum);
            }

            @Override
            public <E extends Enum<E>> void visitEnum(GameRules.Key<EnumRule<E>> key, GameRules.Type<EnumRule<E>> type) {
                EnumRule<E> enumRule = type.createRule();
                addEnumGameRule(key.getName(), language.get(key.getTranslationKey()), enumRule.get(), ((EnumRuleAccessor<E>)(Object) enumRule).getSupportedValues());
            }
        });
    }

    private static JsonObject createSchemaObject(String hashCode) {
        JsonObject schemaObject = new JsonObject();
        schemaObject.addProperty("$schema", "https://json-schema.org/draft/2020-12/schema");
        schemaObject.addProperty("title", "Boring Default Game Rules Configuration File");
        schemaObject.addProperty("gameRulesHash", hashCode);
        schemaObject.addProperty("description", "The config file for the \"Boring Default Game Rules\" mod.");
        schemaObject.addProperty("type", "object");

        JsonObject propertiesObject = new JsonObject();

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
        propertiesObject.add("generate_json_schema", generateJSONSchemaObject);

        schemaObject.add("properties", propertiesObject);

        JsonArray requiredArray = new JsonArray();
        requiredArray.add("default_game_rules");
        requiredArray.add("generate_json_schema");

        schemaObject.add("required", requiredArray);

        return schemaObject;
    }

    private static void addBooleanGameRule(String name, String description, boolean defaultValue) {
        JsonObject booleanGameRuleObject = new JsonObject();
        booleanGameRuleObject.addProperty("type", "boolean");
        booleanGameRuleObject.addProperty("title", name);
        booleanGameRuleObject.addProperty("description", description);
        booleanGameRuleObject.addProperty("default", defaultValue);

        defaultGameRulesProperties.add(name, booleanGameRuleObject);
    }

    private static void addIntegerGameRule(String name, String description, int defaultValue, Optional<Integer> minimum, Optional<Integer> maximum) {
        JsonObject integerGameRuleObject = new JsonObject();
        integerGameRuleObject.addProperty("type", "integer");
        integerGameRuleObject.addProperty("title", name);
        integerGameRuleObject.addProperty("description", description);
        integerGameRuleObject.addProperty("default", defaultValue);
        
        if (!minimum.isEmpty() && minimum.get() != Integer.MIN_VALUE) {
            integerGameRuleObject.addProperty("minimum", minimum.get());
        }

        if (!maximum.isEmpty() && maximum.get() != Integer.MAX_VALUE) {
            integerGameRuleObject.addProperty("maximum", maximum.get());
        }

        defaultGameRulesProperties.add(name, integerGameRuleObject);
    }

    private static void addDoubleGameRule(String name, String description, double defaultValue, double minimum, double maximum) {
        JsonObject doubleGameRuleObject = new JsonObject();
        doubleGameRuleObject.addProperty("type", "number");
        doubleGameRuleObject.addProperty("title", name);
        doubleGameRuleObject.addProperty("description", description);
        doubleGameRuleObject.addProperty("default", defaultValue);

        if (minimum != Double.MIN_NORMAL) {
            doubleGameRuleObject.addProperty("minimum", minimum);
        }

        if (maximum != Integer.MAX_VALUE) {
            doubleGameRuleObject.addProperty("maximum", maximum);
        }

        defaultGameRulesProperties.add(name, doubleGameRuleObject);
    }

    private static <E extends Enum<E>> void addEnumGameRule(String name, String description, E defaultValue, List<E> supportedValues) {
        JsonObject doubleGameRuleObject = new JsonObject();
        doubleGameRuleObject.addProperty("type", "string");
        doubleGameRuleObject.addProperty("title", name);
        doubleGameRuleObject.addProperty("description", description);
        doubleGameRuleObject.addProperty("default", defaultValue.name());
        
        JsonArray supportedEnumValues = new JsonArray();
        for (E supportedValue : supportedValues) {
            supportedEnumValues.add(supportedValue.name());
        }

        doubleGameRuleObject.add("enum", supportedEnumValues);

        defaultGameRulesProperties.add(name, doubleGameRuleObject);
    }

    public static void generateGameRulesHash() {
        GameRulesAccessor.getRuleTypes().keySet().forEach(key -> {
            newSchemaHash += key.getName();
        });
        newSchemaHash = Hashing.sha256().hashString(newSchemaHash, StandardCharsets.UTF_8).toString();
    }
}
