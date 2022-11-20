package argparser.argumentTypes;

import argparser.ArgValueCount;
import argparser.ArgumentType;
import argparser.ErrorLevel;

import java.util.HashMap;

public class KeyValuesArgument<T extends ArgumentType<Ts>, Ts> extends ArgumentType<HashMap<String, Ts>> {
	private final ArgumentType<Ts> valueType;

	public KeyValuesArgument(T type) {
		this.valueType = type;
		this.registerSubType(type);
	}

	@Override
	public ArgValueCount getNumberOfArgValues() {
		return new ArgValueCount(1, -1);
	}

	@Override
	public void parseValues(String[] args) {
		this.value = new HashMap<>();

		this.forEachArgValue(args, arg -> {
			var split = arg.split("=", 2);

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
