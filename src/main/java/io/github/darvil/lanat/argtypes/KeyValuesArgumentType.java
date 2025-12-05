package io.github.darvil.lanat.argtypes;

import io.github.darvil.lanat.ArgumentType;
import io.github.darvil.terminal.textformatter.TextFormatter;
import io.github.darvil.utils.Range;
import io.github.darvil.utils.UtlString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

/**
 * An argument type that takes key-value pairs. The key is a string, and the value is of the argument type given in the
 * constructor. The key-value pairs are separated by an equals sign (e.g. {@code key=value}).
 * <p>
 * The final value of this argument type is a {@link HashMap} of the key-value pairs.
 * </p>
 * @param <Type> The type of the argument type used to parse the values.
 * @param <TInner> The type of the values.
 * @see HashMap
 */
public class KeyValuesArgumentType<Type extends ArgumentType<TInner>, TInner> extends ArgumentType<Map<String, TInner>> {
	private final @NotNull ArgumentType<TInner> valueArgumentType;

	/**
	 * Creates a new key-values argument type.
	 * @param argumentType The argument type used to parse the values.
	 */
	public KeyValuesArgumentType(@NotNull Type argumentType) {
		if (argumentType.getValueCountBounds().start() != 1)
			throw new IllegalArgumentException("The argumentType must at least accept one value.");

		this.valueArgumentType = argumentType;
		this.registerSubType(argumentType);
	}

	@Override
	public @NotNull Range getValueCountBounds() {
		return Range.AT_LEAST_ONE;
	}

	@Override
	public Map<@NotNull String, @NotNull TInner> parseValues(String @NotNull [] values) {
		var map = new Hashtable<String, TInner>();

		this.getArgValuesStream(values)
			.forEach(arg -> {
				final var split = UtlString.split(arg, '=');

				if (split.length != 2) {
					this.addError("Invalid key-value pair: '" + arg + "'.");
					return;
				}

				final var key = split[0];
				final var value = split[1];

				if (key.isBlank()) {
					this.addError("Key cannot be empty.");
					return;
				}

				if (map.containsKey(key)) {
					this.addError("Duplicate key: '" + key + "'.");
					return;
				}

				var valueResult = this.valueArgumentType.parseValues(value);

				if (valueResult == null)
					return;

				map.put(key, valueResult);
			});

		if (map.isEmpty())
			return null;

		return map;
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		return TextFormatter.of("(key=")
			.concat(Objects.requireNonNull(this.valueArgumentType.getRepresentation()), ", ...)");
	}

	@Override
	public @Nullable String getDescription() {
		return "A list of key-value pairs. The key must be a string and the value must be of type " + this.valueArgumentType.getName() + ".";
	}
}