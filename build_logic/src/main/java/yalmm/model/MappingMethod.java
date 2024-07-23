package yalmm.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import yalmm.util.JsonUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MappingMethod extends MappingDescribableEntry {
	private final List<Param> params = new ArrayList<>();

	public MappingMethod(String source, String descriptor) {
		super(source, descriptor);
	}

	public List<Param> getParams() {
		return this.params;
	}

	public record Param(int index, String name, List<String> comments) {
		public boolean hasComments() {
			return this.comments != null && !this.comments.isEmpty();
		}
	}

	public static class Serializer implements JsonDeserializer<MappingMethod> {
		@Override
		public MappingMethod deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			var obj = json.getAsJsonObject();

			String source = obj.get("source").getAsString();
			String descriptor = obj.get("descriptor").getAsString();

			var method = new MappingMethod(source, descriptor);

			if (obj.has("name")) {
				method.setTarget(obj.get("name").getAsString());
			}

			if (obj.has("comments")) {
				method.setComment(context.deserialize(obj.get("comments"), JsonUtils.STRING_LIST_TYPE));
			}

			if (obj.has("params")) {
				var params = obj.get("params").getAsJsonObject();

				for (var entry : params.entrySet()) {
					int index = Integer.parseInt(entry.getKey());
					var paramData = entry.getValue().getAsJsonObject();

					String name = null;
					if (paramData.has("name")) {
						name = paramData.get("name").getAsString();
					}

					List<String> comments = null;
					if (paramData.has("comments")) {
						comments = context.deserialize(paramData.get("comments"), JsonUtils.STRING_LIST_TYPE);
					}

					var param = new Param(index, name, comments);
					method.params.add(param);
				}

				method.params.sort(Comparator.comparingInt(o -> o.index));
			}

			return method;
		}
	}
}
