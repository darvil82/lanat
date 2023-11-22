package lanat.helpRepresentation.descriptions;

import lanat.CommandUser;
import lanat.NamedWithDescription;
import lanat.helpRepresentation.descriptions.exceptions.MalformedTagException;
import lanat.utils.UtlMisc;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DescriptionFormatter {
	private static final char TAG_START = '<';
	private static final char TAG_END = '>';

	private DescriptionFormatter() {}

	/**
	 * Parses the description of the given user and replaces all tags with the content generated by them.
	 *
	 * @param user the user whose description is being parsed
	 * @param desc the description to parse
	 * @return the parsed description
	 */
	public static @NotNull String parse(@NotNull NamedWithDescription user, @NotNull String desc) {
		// if the description doesn't contain any tags, we can skip the parsing
		if (!desc.contains(Character.toString(TAG_START)))
			return desc;

		final var chars = desc.toCharArray();

		final var out = new StringBuilder(); // the output string
		final var currentTag = new StringBuilder(); // the current tag being parsed
		boolean inTag = false; // whether we are currently parsing a tag
		int lastTagOpenIndex = -1; // the index of the last tag start character

		for (int i = 0; i < chars.length; i++) {
			final char chr = chars[i];

			if (chr == '\\') {
				(inTag ? currentTag : out).append(chars[i == chars.length - 1 ? i : ++i]);
			} else if (chr == TAG_END && inTag) {
				if (currentTag.length() == 0)
					throw new MalformedTagException("empty tag at index " + lastTagOpenIndex);

				out.append(DescriptionFormatter.parseTag(currentTag.toString(), user));
				currentTag.setLength(0);
				inTag = false;
			} else if (chr == TAG_START && !inTag) {
				inTag = true;
				lastTagOpenIndex = i;
			} else {
				(inTag ? currentTag : out).append(chr);
			}
		}

		if (inTag) {
			throw new IllegalArgumentException("unclosed tag at index " + lastTagOpenIndex);
		}

		return out.toString();
	}

	/**
	 * Parses the description of the given user and replaces all tags with the content generated by them. The
	 * description is taken from the given user by calling {@link NamedWithDescription#getDescription()}.
	 *
	 * @param element the user whose description is being parsed
	 * @param <T> the type of the user
	 * @return the parsed description, or null if the user has no description
	 * @see #parse(NamedWithDescription, String)
	 */
	public static <T extends CommandUser & NamedWithDescription>
	@Nullable String parse(@NotNull T element) {
		return UtlMisc.nullOrElseGet(
			element.getDescription(),
			desc -> DescriptionFormatter.parse(element, desc)
		);
	}

	/**
	 * Parses the given tag and returns the content generated by its parser.
	 *
	 * @param tagContents the contents of the tag, excluding the tag start and end characters
	 * @param user the user whose description is being parsed
	 * @return the content generated by the tag
	 */
	private static @NotNull String parseTag(@NotNull String tagContents, @NotNull NamedWithDescription user) {
		if (tagContents.contains("=")) {
			final var split = UtlString.split(tagContents, '=', 2);
			return Tag.parseTagValue(user, split[0], split[1]);
		}

		return Tag.parseTagValue(user, tagContents, null);
	}

}