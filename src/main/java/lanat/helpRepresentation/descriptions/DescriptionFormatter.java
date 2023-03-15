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

	/**
	 * Parses the description of the given user and replaces all tags with the content generated by them.
	 * @param user the user whose description is being parsed
	 * @param desc the description to parse
	 * @return the parsed description
	 */
	public static @NotNull String parse(@NotNull NamedWithDescription user, @NotNull String desc) {
		// if the description doesn't contain any tags, we can skip the parsing
		if (!desc.contains(Character.toString(TAG_START)) && !desc.contains(Character.toString(TAG_END)))
			return desc;

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

				out.append(DescriptionFormatter.parseTag(current.toString(), user));
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

	/**
	 * Parses the description of the given user and replaces all tags with the content generated by them.
	 * The description is taken from the given user by calling {@link NamedWithDescription#getDescription()}.
	 * @param element the user whose description is being parsed
	 * @return the parsed description, or null if the user has no description
	 * @param <T> the type of the user
	 * @see #parse(NamedWithDescription, String)
	 */
	public static <T extends CommandUser & NamedWithDescription>
	@Nullable String parse(@NotNull T element) {
		final var desc = element.getDescription();
		if (desc == null)
			return null;

		return DescriptionFormatter.parse(element, desc);
	}

	/**
	 * Parses the given tag and returns the content generated by its parser.
	 * @param tagContents the contents of the tag, excluding the tag start and end characters
	 * @param user the user whose description is being parsed
	 * @return the content generated by the tag
	 */
	private static @NotNull String parseTag(@NotNull String tagContents, @NotNull NamedWithDescription user) {
		if (tagContents.contains("=")) {
			final var split = tagContents.split("=", 2);
			return Tag.parseTagValue(user, split[0].trim(), split[1].trim());
		}

		return Tag.parseTagValue(user, tagContents, null);
	}

}