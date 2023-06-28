package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.Range;
import lanat.utils.UtlString;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;

public class KeyValuesArgumentType<T extends ArgumentType<Ts>, Ts> extends ArgumentType<HashMap<String, Ts>> {
	private final @NotNull ArgumentType<Ts> valueType;

	public KeyValuesArgumentType(@NotNull T type) {
		if (type.getRequiredArgValueCount().min() != 1)
			throw new IllegalArgumentException("The value type must at least accept one value.");

		this.valueType = type;
		this.registerSubType(type);
	}

	@Override
	public @NotNull Range getRequiredArgValueCount() {
		return Range.AT_LEAST_ONE;
	}

	@Override
	public HashMap<@NotNull String, @NotNull Ts> parseValues(String @NotNull [] args) {
		HashMap<String, Ts> tempHashMap = new HashMap<>();

		this.forEachArgValue(args, arg -> {
			final var split = UtlString.split(arg, '=');

			if (split.length != 2) {
				this.addError("Invalid key-value pair: '" + arg + "'.");
				return;
			}

			final var key = split[0];
			final var value = split[1];

			if (key.isEmpty()) {
				this.addError("Key cannot be empty.");
				return;
			}

			if (tempHashMap.containsKey(key)) {
				this.addError("Duplicate key: '" + key + "'.");
				return;
			}

			this.valueType.parseAndUpdateValue(value);
			tempHashMap.put(key, this.valueType.getFinalValue());
		});

		if (tempHashMap.isEmpty())
			return null;

		return tempHashMap;
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		return new TextFormatter("(key=")
			.concat(Objects.requireNonNull(this.valueType.getRepresentation()))
			.concat(", ...)");
	}

	@Override
	public @Nullable String getDescription() {
		return "A list of key-value pairs. The key must be a string and the value must be of type " + this.valueType.getName() + ".";
	}
}
