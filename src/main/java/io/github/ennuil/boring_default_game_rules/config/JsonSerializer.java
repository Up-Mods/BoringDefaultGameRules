/*
 * Copyright 2022-2024 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ennuil.boring_default_game_rules.config;

import org.quiltmc.config.api.Config;
import org.quiltmc.config.api.MarshallingUtils;
import org.quiltmc.config.api.Serializer;
import org.quiltmc.config.api.exceptions.ConfigParseException;
import org.quiltmc.config.api.values.*;
import org.quiltmc.config.impl.tree.TrackedValueImpl;
import org.quiltmc.config.impl.util.SerializerUtils;
import org.quiltmc.parsers.json.JsonReader;
import org.quiltmc.parsers.json.JsonToken;
import org.quiltmc.parsers.json.JsonWriter;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// A JSONfied version of Quilt Config's Json5Serializer
// TODO - Contribute something to upstream!
public final class JsonSerializer implements Serializer {
	public JsonSerializer() {}

	@Override
	public String getFileExtension() {
		return "json";
	}

	private void serialize(JsonWriter writer, Object value) throws IOException {
		if (value instanceof Integer intValue) {
			writer.value(intValue);
		} else if (value instanceof Long longValue) {
			writer.value(longValue);
		} else if (value instanceof Float floatValue) {
			writer.value(floatValue);
		} else if (value instanceof Double doubleValue) {
			writer.value(doubleValue);
		} else if (value instanceof Boolean booleanValue) {
			writer.value(booleanValue);
		} else if (value instanceof String stringValue) {
			writer.value(stringValue);
		} else if (value instanceof BigInteger bigIntValue) {
			writer.value(bigIntValue);
		} else if (value instanceof BigDecimal bigDecimalValue) {
			writer.value(bigDecimalValue);
		} else if (value instanceof ValueList<?> valueList) {
			writer.beginArray();

			for (var v : valueList) {
				serialize(writer, v);
			}

			writer.endArray();
		} else if (value instanceof ValueMap<?>) {
			writer.beginObject();

			for (Map.Entry<String, ?> entry : (ValueMap<?>) value) {
				writer.name(entry.getKey());
				serialize(writer, entry.getValue());
			}

			writer.endObject();
		} else if (value instanceof ConfigSerializableObject<?> serializableObject) {
			serialize(writer, serializableObject.getRepresentation());
		} else if (value == null) {
			writer.nullValue();
		} else if (value.getClass().isEnum()) {
			writer.value(((Enum<?>) value).name());
		} else {
			throw new ConfigParseException();
		}
	}

	private void serialize(JsonWriter writer, ValueTreeNode node) throws IOException {
		if (node instanceof ValueTreeNode.Section) {
			writer.name(node.key().getLastComponent());
			writer.beginObject();

			for (ValueTreeNode child : ((ValueTreeNode.Section) node)) {
				serialize(writer, child);
			}

			writer.endObject();
		} else {
			TrackedValue<?> trackedValue = ((TrackedValue<?>) node);
			writer.name(SerializerUtils.getSerializedName(trackedValue));

			serialize(writer, trackedValue.getRealValue());
		}
	}

	@Override
	public void serialize(Config config, OutputStream to) throws IOException {
		JsonWriter writer = JsonWriter.json(new OutputStreamWriter(to));

		writer.beginObject();

		for (ValueTreeNode node : config.nodes()) {
			this.serialize(writer, node);
		}

		writer.endObject();
		writer.close();
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void deserialize(Config config, InputStream from) {
		try {
			var reader = JsonReader.json(new InputStreamReader(from));

			Map<String, Object> values = parseObject(reader);

			for (TrackedValue<?> value : config.values()) {
				Map<String, Object> m = values;
				List<ValueKey> keyOptions = SerializerUtils.getPossibleKeys(config, value);

				for (ValueKey key : keyOptions) {
					for (int i = 0; i < key.length(); i++) {
						String name = key.getKeyComponent(i);
						if (m.containsKey(name) && i != key.length() - 1) {
							m = (Map<String, Object>) m.get(name);
						} else if (m.containsKey(name)) {
							((TrackedValueImpl) value).setValue(MarshallingUtils.coerce(m.get(name), value.getDefaultValue(), (Map<String, ?> map, MarshallingUtils.MapEntryConsumer entryConsumer) ->
								map.forEach(entryConsumer::put)), false);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Map<String, Object> parseObject(JsonReader reader) throws IOException {
		reader.beginObject();

		Map<String, Object> object = new LinkedHashMap<>();

		while (reader.hasNext() && reader.peek() == JsonToken.NAME) {
			object.put(reader.nextName(), parseElement(reader));
		}

		reader.endObject();

		return object;
	}

	public static List<Object> parseArray(JsonReader reader) throws IOException {
		reader.beginArray();

		List<Object> array = new ArrayList<>();

		while (reader.hasNext() && reader.peek() != JsonToken.END_ARRAY) {
			array.add(parseElement(reader));
		}

		reader.endArray();

		return array;
	}

	private static Object parseElement(JsonReader reader) throws IOException {
		return switch (reader.peek()) {
			case END_ARRAY -> throw new ConfigParseException("Unexpected end of array");
			case BEGIN_OBJECT -> parseObject(reader);
			case BEGIN_ARRAY ->	parseArray(reader);
			case END_OBJECT -> throw new ConfigParseException("Unexpected end of object");
			case NAME -> throw new ConfigParseException("Unexpected name");
			case STRING -> reader.nextString();
			case NUMBER -> reader.nextNumber();
			case BOOLEAN -> reader.nextBoolean();
			case NULL -> {
				reader.nextNull();
				yield null;
			}
			case END_DOCUMENT -> throw new ConfigParseException("Unexpected end of file");
			default -> throw new ConfigParseException("Encountered unknown JSON token");
		};
	}
}
