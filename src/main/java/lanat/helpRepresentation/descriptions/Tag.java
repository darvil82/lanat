package lanat.helpRepresentation.descriptions;

import lanat.NamedWithDescription;
import lanat.helpRepresentation.descriptions.exceptions.UnknownTagException;
import lanat.helpRepresentation.descriptions.tags.ColorTag;
import lanat.helpRepresentation.descriptions.tags.DescTag;
import lanat.helpRepresentation.descriptions.tags.FormatTag;
import lanat.helpRepresentation.descriptions.tags.LinkTag;
import lanat.utils.UtlReflection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Hashtable;



/**
 * Class for handling parsing of the simple tags used in descriptions. (e.g. {@code <a-tag=the-value>}).
 * Tags may receive no value, in which case the value received by the {@link #parse(NamedWithDescription, String)}
 * method will be {@code null}.
 * @see #parse(NamedWithDescription, String)
 */
public abstract class Tag {
	private static final Hashtable<String, Class<? extends Tag>> registeredTags = new Hashtable<>();


	/**
	 * This method will parse the tag value and return the parsed value.
	 * @param user user that is parsing the tag
	 * @param value value of the tag. May be {@code null} if the tag has no value specified. (e.g. {@code <a-tag>})
	 * @return parsed value of the tag
	 * @see Tag
	 */
	protected abstract @NotNull String parse(@NotNull NamedWithDescription user, @Nullable String value);


	/** Initialize the tags. This method will register the default tags that are used in descriptions. */
	public static void initTags() {
		Tag.registerTag("link", LinkTag.class);
		Tag.registerTag("desc", DescTag.class);
		Tag.registerTag("color", ColorTag.class);
		Tag.registerTag("format", FormatTag.class);
	}

	/**
	 * Register a tag class to be used in descriptions. This class will be instantiated to parse
	 * the tags encountered in the descriptions being parsed.
	 * @param name name of the tag (case-insensitive)
	 * @param tag tag object that will be used to parse the tag
	 */
	public static void registerTag(@NotNull String name, @NotNull Class<? extends Tag> tag) {
		if (name.isEmpty()) throw new IllegalArgumentException("Tag name cannot be empty");
		Tag.registeredTags.put(name, tag);
	}

	/**
	 * Parse a tag value. This method will parse the tag value using the tag registered with the given name.
	 * @param user user that is parsing the tag
	 * @param tagName name of the tag
	 * @param value value of the tag
	 * @return parsed value of the tag
	 */
	static @NotNull String parseTagValue(
		@NotNull NamedWithDescription user,
		@NotNull String tagName,
		@Nullable String value
	) {
		final var tagClass = Tag.registeredTags.get(tagName.toLowerCase());

		if (tagClass == null)
			throw new UnknownTagException(tagName);

		return UtlReflection.instantiate(tagClass).parse(user, value);
	}
}