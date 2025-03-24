package yalmm.enigma.index;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonToken;
import yalmm.util.CasingUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class SimpleTypeParamNamesRegistry {
	private final Path path;
	/**
	 * Using a {@link LinkedHashMap} to ensure we keep the read order.
	 */
	private final Map<String, Entry> entries = new LinkedHashMap<>();

	public SimpleTypeParamNamesRegistry(Path path) {
		this.path = path;
	}

	public @Nullable Entry getEntry(String type) {
		return this.entries.get(type);
	}

	public void read() {
		try (var reader = JsonReader.json5(this.path)) {
			if (reader.peek() != JsonToken.BEGIN_OBJECT) {
				return;
			}

			reader.beginObject();

			while (reader.hasNext()) {
				String type = reader.nextName();

				switch (reader.peek()) {
					case STRING -> {
						String localName = reader.nextString();
						this.entries.put(type, new Entry(type, localName, CasingUtils.toScreamingSnakeCase(localName)));
					}
					case BEGIN_OBJECT -> {
						String localName = null;
						String staticName = null;
						boolean exclusive = false;
						List<Name> fallback = Collections.emptyList();

						reader.beginObject();

						while (reader.hasNext()) {
							String key = reader.nextName();

							switch (key) {
								case "local_name" -> localName = reader.nextString();
								case "static_name" -> staticName = reader.nextString();
								case "exclusive" -> exclusive = reader.nextBoolean();
								case "fallback" -> {
									reader.beginArray();

									fallback = this.collectFallbacks(reader, type);

									reader.endArray();
								}
								default -> reader.skipValue();
							}
						}

						reader.endObject();

						if (localName == null) {
							System.err.println("Failed parsing local name for type " + type);
							break;
						}

						if (staticName == null) staticName = CasingUtils.toScreamingSnakeCase(localName);

						this.entries.put(type, new Entry(type, new Name(localName, staticName), exclusive, fallback));
					}
					default -> reader.skipValue();
				}
			}

			reader.endObject();
		} catch (IOException e) {
			System.err.println("Failed to read simple type field names registry.");
			e.printStackTrace();
		}
	}

	private List<Name> collectFallbacks(JsonReader reader, String type) throws IOException {
		var list = new ArrayList<Name>();

		while (reader.hasNext()) {
			switch (reader.peek()) {
				case STRING -> {
					String name = reader.nextString();
					list.add(new Name(name, CasingUtils.toScreamingSnakeCase(name)));
				}
				case BEGIN_OBJECT -> {
					String localName = null;
					String staticName = null;
					reader.beginObject();

					while (reader.hasNext()) {
						String key = reader.nextName();

						switch (key) {
							case "local_name" -> localName = reader.nextString();
							case "static_name" -> staticName = reader.nextString();
							default -> reader.skipValue();
						}
					}

					reader.endObject();

					if (localName == null) {
						System.err.println("Failed parsing fallback local name for type " + type);
						break;
					}

					if (staticName == null) staticName = CasingUtils.toScreamingSnakeCase(localName);

					list.add(new Name(localName, staticName));
				}
			}
		}

		return list;
	}

	public record Entry(String type, Name name, boolean exclusive, List<Name> fallback) {
		public Entry(String type, String localName, String staticName) {
			this(type, new Name(localName, staticName), false, Collections.emptyList());
		}

		public @Nullable Name findFallback(Predicate<Name> predicate) {
			for (var fallback : this.fallback) {
				if (predicate.test(fallback))
					return fallback;
			}

			return null;
		}
	}

	public record Name(String local, String staticName) {
	}
}