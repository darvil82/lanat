package lanat.argumentTypes;

import lanat.ArgValueCount;
import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;

import java.util.HashMap;

public class KeyValuesArgument<T extends ArgumentType<Ts>, Ts> extends ArgumentType<HashMap<String, Ts>> {
	private final ArgumentType<Ts> valueType;
	private final char separator;

	public KeyValuesArgument(T type, char separator) {
		this.valueType = type;
		this.separator = separator;
		this.registerSubType(type);
	}

	public KeyValuesArgument(T type) {
		this(type, '=');
	}

	@Override
	public ArgValueCount getNumberOfArgValues() {
		return new ArgValueCount(1, -1);
	}

	@Override
	public HashMap<String, Ts> parseValues(String[] args) {
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
	public TextFormatter getRepresentation() {
		return new TextFormatter("(key=").concat(this.valueType.getRepresentation()).concat(", ...)");
	}

	public static <T extends ArgumentType<Ts>, Ts> KeyValuesArgument<T, Ts> create(T type, char separator) {
		return new KeyValuesArgument<>(type, separator);
	}
}
