package lanat.helpRepresentation.descriptions;

import lanat.CommandUser;
import lanat.NamedWithDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DescriptionFormatter {
	private DescriptionFormatter() {}

	public static @Nullable <T extends CommandUser & NamedWithDescription>
	String parse(@NotNull T element) {
		final var desc = element.getDescription();
		if (desc == null)
			return null;

		final var chars = desc.toCharArray();

		final var out = new StringBuilder();
		final var current = new StringBuilder();

		for (int i = 0; i < chars.length; i++) {
			final char c = chars[i];

			if (c == '>' && !current.isEmpty()) {
				out.append(parseTag(current.toString(), element));
				current.setLength(0);
			} else if (c == '<' && current.isEmpty()) {
				current.append(chars[++i]);
			} else if (!current.isEmpty()) {
				current.append(c);
			} else if (c == '\\') {
				out.append(chars[++i]);
			} else {
				out.append(c);
			}
		}

		return out.toString();
	}

	private static @NotNull String parseTag(@NotNull String tag, @NotNull CommandUser user) {
		final var split = tag.split("=", 2);
		final var tagName = split[0];
		final var tagValue = split[1];

		return tagValue;
	}

}