package lanat.helpRepresentation.descriptions;

import lanat.CommandUser;
import lanat.NamedWithDescription;
import lanat.helpRepresentation.descriptions.exceptions.MalformedTagException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DescriptionFormatter {
	private static final char TAG_START = '<';
	private static final char TAG_END = '>';

	private DescriptionFormatter() {}

	public static @Nullable <T extends CommandUser & NamedWithDescription>
	String parse(@NotNull T element) {
		final var desc = element.getDescription();
		if (desc == null)
			return null;

		final var chars = desc.toCharArray();

		final var out = new StringBuilder();
		final var current = new StringBuilder();
		boolean inTag = false;
		int lastTagOpen = -1;

		for (int i = 0; i < chars.length; i++) {
			final char chr = chars[i];

			if (chr == TAG_END && inTag) {
				if (current.length() == 0)
					throw new MalformedTagException("empty tag at index " + lastTagOpen);

				out.append(DescriptionFormatter.parseTag(current.toString(), element));
				current.setLength(0);
				inTag = false;
			} else if (chr == TAG_START && !inTag) {
				inTag = true;
				lastTagOpen = i;
			} else if (inTag) {
				current.append(chr);
			} else if (chr == '\\') {
				out.append(chars[i == chars.length - 1 ? i : ++i]);
			} else {
				out.append(chr);
			}
		}

		if (inTag) {
			throw new IllegalArgumentException("unclosed tag at index " + lastTagOpen);
		}

		return out.toString();
	}


	private static <T extends CommandUser & NamedWithDescription>
	@NotNull String parseTag(@NotNull String tagContents, @NotNull T user) {
		if (tagContents.contains("=")) {
			final var split = tagContents.split("=", 2);
			return Tag.parseTagValue(split[0].trim(), split[1].trim(), user);
		}

		return Tag.parseTagValue(tagContents, "", user);
	}

}