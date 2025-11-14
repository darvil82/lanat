package io.github.darvil.lanat.helpgen.descriptions.tags;

import io.github.darvil.lanat.helpgen.descriptions.Tag;
import io.github.darvil.lanat.helpgen.descriptions.exceptions.MalformedTagException;
import io.github.darvil.lanat.utils.NamedWithDescription;
import io.github.darvil.terminal.textformatter.TextFormatter;
import io.github.darvil.terminal.textformatter.color.Color;
import io.github.darvil.terminal.textformatter.color.SimpleColor;
import io.github.darvil.terminal.textformatter.color.TrueColor;
import io.github.darvil.utils.UtlString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Changes the color of the text.
 * <p>
 * The syntax for specifying colors is {@code foreground[:background]} or {@code [foreground]:background},
 * where {@code foreground} and {@code background} follow the syntax {@code color_name|#rrggbb|r,g,b}.
 * If the background is specified, the foreground may be left blank.
 * <ul>
 * <li>{@code #rrggbb} is the hexadecimal value of the color.</li>
 * <li>{@code r,g,b} are the RGB values of the color.</li>
 * <li>
 * {@code color_name} is a case-insensitive color name.
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
 * </li>
 * </ul>
 *
 * If the color value is invalid, a {@link MalformedTagException} is thrown.
 * @see TrueColor
 * @see SimpleColor
 */
public class ColorTag extends Tag {
	@Override
	protected @NotNull String parse(@NotNull NamedWithDescription user, @Nullable String value) {
		if (value == null)
			throw new MalformedTagException(ColorTag.class, "no color specified");

		// if color sequences are disabled, nothing to do
		if (!TextFormatter.enableSequences) return "";

		if (!value.contains(":")) return ColorTag.getColor(value).fg();

		final String[] split = UtlString.split(value, ':');
		if (split.length != 2)
			throw new MalformedTagException(
				ColorTag.class, "invalid color format '" + value + "' (expected format: '[foreground]:background')"
			);

		return (split[0].isEmpty() ? "" : ColorTag.getColor(split[0]).fg()) + ColorTag.getColor(split[1]).bg();
	}

	private static @NotNull Color getColor(@NotNull String colorName) {
		if (colorName.contains(",") || colorName.startsWith("#"))
			return ColorTag.getTrueColor(colorName);

		return ColorTag.getSimpleColor(colorName);
	}

	private static @NotNull SimpleColor getSimpleColor(@NotNull String colorName) {
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

	private static @NotNull TrueColor getTrueColor(@NotNull String color) {
		// if the color starts with a '#', it's a hex color
		if (color.startsWith("#")) {
			var hex = color.substring(1);
			if (hex.length() != 6)
				throw new MalformedTagException(ColorTag.class, "invalid hex color '" + color + "'. Expected format: '#rrggbb'");
			return TrueColor.of(Integer.parseInt(hex, 16));
		}

		// otherwise, just try splitting the string by commas
		var split = UtlString.split(color, ',');
		if (split.length != 3)
			throw new MalformedTagException(ColorTag.class, "invalid RGB color '" + color + "'. Expected format: 'r,g,b'");

		try {
			return TrueColor.of(
				Integer.parseInt(split[0]),
				Integer.parseInt(split[1]),
				Integer.parseInt(split[2])
			);
		} catch (NumberFormatException e) {
			throw new MalformedTagException(ColorTag.class, "invalid color value. " + e.getMessage());
		}
	}
}