package argparser.argumentTypes;

import argparser.ArgValueCount;
import argparser.ArgumentType;

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
	public void parseValues(String[] args) {
		this.value = new HashMap<>();

		this.forEachArgValue(args, arg -> {
			var split = arg.split("=");

			if (split.length != 2) {
				this.addError("Invalid key-value pair: '" + arg + "'.");
				return;
			}

			var key = split[0].strip();
			var value = split[1].strip();

			if (key.isEmpty()) {
				this.addError("Key cannot be empty.");
				return;
			}

			if (this.value.containsKey(key)) {
				this.addError("Duplicate key: '" + key + "'.");
				return;
			}

			this.valueType.parseValues(value);
			this.value.put(key, this.valueType.getFinalValue());
		});
	}
}
