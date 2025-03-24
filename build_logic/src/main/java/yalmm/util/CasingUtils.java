package yalmm.util;

public final class CasingUtils {
	public static String toScreamingSnakeCase(String name) {
		var builder = new StringBuilder();

		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);

			if (c == '_') {
				builder.append(c);
			} else {
				if (Character.isUpperCase(c) && i != 0) {
					builder.append('_');
				}

				builder.append(Character.toUpperCase(c));
			}
		}

		return builder.toString();
	}
}
