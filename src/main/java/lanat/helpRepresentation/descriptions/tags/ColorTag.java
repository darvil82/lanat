package lanat.helpRepresentation.descriptions.tags;

import lanat.helpRepresentation.descriptions.Tag;
import lanat.helpRepresentation.descriptions.exceptions.MalformedTagException;
import lanat.utils.NamedWithDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.TextFormatter;
import textFormatter.color.Color;
import textFormatter.color.SimpleColor;
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
			case "black" -> SimpleColor.BLACK;
			case "red" -> SimpleColor.BRIGHT_RED;
			case "green" -> SimpleColor.BRIGHT_GREEN;
			case "yellow" -> SimpleColor.BRIGHT_YELLOW;
			case "blue" -> SimpleColor.BRIGHT_BLUE;
			case "magenta" -> SimpleColor.BRIGHT_MAGENTA;
			case "cyan" -> SimpleColor.BRIGHT_CYAN;
			case "white" -> SimpleColor.BRIGHT_WHITE;
			case "gray", "grey" -> SimpleColor.GRAY;
			case "dark red" -> SimpleColor.RED;
			case "dark green" -> SimpleColor.GREEN;
			case "dark yellow" -> SimpleColor.YELLOW;
			case "dark blue" -> SimpleColor.BLUE;
			case "dark magenta" -> SimpleColor.MAGENTA;
			case "dark cyan" -> SimpleColor.CYAN;
			case "dark white" -> SimpleColor.WHITE;
			default -> throw new MalformedTagException(ColorTag.class, "unknown color name '" + colorName + "'");
		};
	}
}