package lanat.helpRepresentation.descriptions.tags;

import lanat.NamedWithDescription;
import lanat.helpRepresentation.descriptions.Tag;
import lanat.helpRepresentation.descriptions.exceptions.MalformedTagException;
import lanat.utils.UtlString;
import lanat.utils.displayFormatter.Color;
import lanat.utils.displayFormatter.FormatOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Changes the color of the text.
 * The available colors are the ones defined in {@link Color}. The color name is case-insensitive.
 * If the color name is invalid, a {@link MalformedTagException} is thrown.
 * If no color is specified, the reset sequence is returned.
 * */
public class ColorTag extends Tag {
	@Override
	protected @NotNull String parse(@NotNull NamedWithDescription user, @Nullable String value) {
		if (value == null) return FormatOption.RESET_ALL.toString();

		for (Color color : Color.class.getEnumConstants()) {
			if (color.name().equalsIgnoreCase(value)) {
				return color.toString();
			}
		}
		throw new MalformedTagException("color", "unknown color name " + UtlString.surround(value));
	}
}
