package lanat.argumentTypes;

import lanat.utils.Range;
import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

public class KeyValuesArgument<T extends ArgumentType<Ts>, Ts> extends ArgumentType<HashMap<String, Ts>> {
	private final @NotNull ArgumentType<Ts> valueType;
	private final char separator;

	public KeyValuesArgument(@NotNull T type, char separator) {
		if (type.getRequiredArgValueCount().min != 1)
			throw new IllegalArgumentException("The value type must at least accept one value.");

		this.valueType = type;
		this.separator = separator;
		this.registerSubType(type);
	}

	public KeyValuesArgument(@NotNull T type) {
		this(type, '=');
	}

	@Override
	public @NotNull Range getRequiredArgValueCount() {
		return new Range(1, -1);
	}

	@Override
	public HashMap<@NotNull String, @NotNull Ts> parseValues(String @NotNull [] args) {
		HashMap<String, Ts> tempHashMap = new HashMap<>();

		this.forEachArgValue(args, arg -> {
			final var split = arg.split("\\%c".formatted(this.separator));

			if (split.length != 2) {
				this.addError("Invalid key-value pair: '" + arg + "'.");
				return;
			}

			final var key = split[0].strip();
			final var value = split[1].strip();

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

	public static <T extends ArgumentType<Ts>, Ts> KeyValuesArgument<T, Ts> create(T type, char separator) {
		return new KeyValuesArgument<>(type, separator);
	}
}
