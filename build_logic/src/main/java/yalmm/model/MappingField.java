package yalmm.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import yalmm.util.JsonUtils;

import java.lang.reflect.Type;

public class MappingField extends MappingDescribableEntry {
	public MappingField(String source, String descriptor) {
		super(source, descriptor);
	}

	public static class Serializer implements JsonDeserializer<MappingField> {
		@Override
		public MappingField deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			var obj = json.getAsJsonObject();

			String source = obj.get("source").getAsString();
			String descriptor = obj.get("descriptor").getAsString();

			var field = new MappingField(source, descriptor);

			if (obj.has("name")) {
				field.setTarget(obj.get("name").getAsString());
			}

			if (obj.has("comments")) {
				field.setComment(context.deserialize(obj.get("comments"), JsonUtils.STRING_LIST_TYPE));
			}

			return field;
		}
	}
}
