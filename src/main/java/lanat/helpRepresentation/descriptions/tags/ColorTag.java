package lanat.helpRepresentation.descriptions.tags;

import lanat.helpRepresentation.descriptions.Tag;
import lanat.helpRepresentation.descriptions.exceptions.MalformedTagException;
import lanat.utils.NamedWithDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.Color;
import textFormatter.TextFormatter;
import utils.UtlString;

/**
 * Changes the color of the text. (e.g. {@code <color=red>}). The available colors are the ones defined in
 * {@link Color}. The color name is case-insensitive.
 * <p>
 * The values that may be used are:
 * <ul>
 * <li>black</li>
 * <li>red</li>
 * <li>green</li>
 * <li>yellow</li>
 * <li>blue</li>
 * <li>magenta</li>
 * <li>cyan</li>
 * <li>white</li>
 * <li>gray / grey</li>
 * <li>dark red</li>
 * <li>dark green</li>
 * <li>dark yellow</li>
 * <li>dark blue</li>
 * <li>dark magenta</li>
 * <li>dark cyan</li>
 * <li>dark white</li>
 * </ul>
 *
 * <p>
 * The tag may receive a single color name, in which case the color will be applied to the foreground.
 * If the tag receives two color names, separated by a colon, the first color will be applied to the foreground and the
 * second color will be applied to the background. (e.g. {@code <color=red:blue>}).
 * </p>
 * <p>
 * If the color name is invalid, a {@link MalformedTagException} is thrown.
 * </p>
 * @see Color
 */
public class ColorTag extends Tag {
	@Override
	protected @NotNull String parse(@NotNull NamedWithDescription user, @Nullable String value) {
		if (!TextFormatter.enableSequences) return "";
		if (value == null)
			throw new MalformedTagException(ColorTag.class, "no color specified");

		if (!value.contains(":")) return ColorTag.getColor(value).fg();

		final String[] split = UtlString.split(value, ':');
		if (split.length != 2)
			throw new MalformedTagException(
				ColorTag.class, "invalid color format '" + value + "' (expected format: 'foreground:background')"
			);

		return ColorTag.getColor(split[0]).fg() + ColorTag.getColor(split[1]).bg();
	}

	private static Color getColor(@NotNull String colorName) {
		return switch (colorName.toLowerCase().strip()) {
			case "black" -> Color.BLACK;
			case "red" -> Color.BRIGHT_RED;
			case "green" -> Color.BRIGHT_GREEN;
			case "yellow" -> Color.BRIGHT_YELLOW;
			case "blue" -> Color.BRIGHT_BLUE;
			case "magenta" -> Color.BRIGHT_MAGENTA;
			case "cyan" -> Color.BRIGHT_CYAN;
			case "white" -> Color.BRIGHT_WHITE;
			case "gray", "grey" -> Color.GRAY;
			case "dark red" -> Color.RED;
			case "dark green" -> Color.GREEN;
			case "dark yellow" -> Color.YELLOW;
			case "dark blue" -> Color.BLUE;
			case "dark magenta" -> Color.MAGENTA;
			case "dark cyan" -> Color.CYAN;
			case "dark white" -> Color.WHITE;
			default -> throw new MalformedTagException(ColorTag.class, "unknown color name '" + colorName + "'");
		};
	}
}