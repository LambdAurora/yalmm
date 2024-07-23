package yalmm.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import yalmm.util.JsonUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MappingClass extends MappingEntry {
	private final List<MappingField> fields = new ArrayList<>();
	private final List<MappingMethod> methods = new ArrayList<>();
	private final List<MappingClass> subClasses = new ArrayList<>();

	public MappingClass(String source) {
		super(source);
	}

	public List<MappingField> getFields() {
		return this.fields;
	}

	public List<MappingMethod> getMethods() {
		return this.methods;
	}

	public List<MappingClass> getSubClasses() {
		return this.subClasses;
	}

	public static class Serializer implements JsonDeserializer<MappingClass> {
		@Override
		public MappingClass deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			var obj = json.getAsJsonObject();

			String source = obj.get("source").getAsString();
			var clazz = new MappingClass(source);

			if (obj.has("name")) {
				clazz.setTarget(obj.get("name").getAsString());
			}

			if (obj.has("components")) {
				// For records.
				var components = obj.get("components").getAsJsonArray();

				for (var component : components) {
					MappingField field = context.deserialize(component, MappingField.class);
					clazz.getFields().add(field);
					var method = new MappingMethod(field.getSource(), "()" + field.getDescriptor());
					method.setTarget(field.getTarget());
					method.setComment(field.getComment());
					clazz.getMethods().add(method);
				}
			}

			if (obj.has("fields")) {
				var fields = obj.get("fields").getAsJsonArray();

				for (var field : fields) {
					clazz.getFields().add(context.deserialize(field, MappingField.class));
				}
			}

			if (obj.has("methods")) {
				var methods = obj.get("methods").getAsJsonArray();

				for (var method : methods) {
					clazz.getMethods().add(context.deserialize(method, MappingMethod.class));
				}
			}

			if (obj.has("classes")) {
				var subClasses = obj.get("classes").getAsJsonArray();

				for (var subClass : subClasses) {
					clazz.getSubClasses().add(context.deserialize(subClass, MappingClass.class));
				}
			}

			if (obj.has("comments")) {
				clazz.setComment(context.deserialize(obj.get("comments"), JsonUtils.STRING_LIST_TYPE));
			}

			return clazz;
		}
	}
}
