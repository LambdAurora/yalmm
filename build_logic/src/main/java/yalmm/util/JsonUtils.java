package yalmm.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import yalmm.model.MappingClass;
import yalmm.model.MappingField;
import yalmm.model.MappingMethod;

import java.lang.reflect.Type;
import java.util.List;

public final class JsonUtils {
	public static final Type STRING_LIST_TYPE = TypeToken.getParameterized(List.class, String.class).getType();

	public static final Gson GSON = new GsonBuilder()
			.setStrictness(Strictness.LENIENT)
			.registerTypeAdapter(STRING_LIST_TYPE,
					(JsonDeserializer<List<String>>) (json, typeOfT, context) -> {
						if (json.isJsonArray()) {
							return json.getAsJsonArray().asList()
									.stream()
									.filter(JsonElement::isJsonPrimitive)
									.map(JsonElement::getAsString)
									.toList();
						} else if (json.isJsonPrimitive()) {
							return List.of(json.getAsString());
						} else {
							return List.of();
						}
					}
			)
			.registerTypeAdapter(MappingField.class, new MappingField.Serializer())
			.registerTypeAdapter(MappingMethod.class, new MappingMethod.Serializer())
			.registerTypeAdapter(MappingClass.class, new MappingClass.Serializer())
			.create();
}
