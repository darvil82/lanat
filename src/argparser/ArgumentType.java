package argparser;

import java.util.Arrays;

public abstract class ArgumentType<T> {
	protected T value;

	public abstract void parseArgValues(String[] args);
	public byte getNumberOfArgValues() {
		return 1;
	}
	public String getRepresentation() {
		return this.getClass().getName();
	};
	public T getFinalValue() {
		return this.value;
	}

	// Easy to access values. These are methods because we don't want to use the same instance everywhere.
	public static IntArgument INTEGER() { return new IntArgument(); }
	public static BooleanArgument BOOLEAN() { return new BooleanArgument(); }
}


class IntArgument extends ArgumentType<Integer> {
	@Override
	public void parseArgValues(String[] arg) {
		this.value = Integer.parseInt(arg[0]);
	}

	@Override
	public String getRepresentation() {
		return "int";
	}
}


class BooleanArgument extends ArgumentType<Boolean> {
	@Override
	public void parseArgValues(String[] arg) {
		this.value = true;
	}

	@Override
	public String getRepresentation() {
		return "bool";
	}

	@Override
	// this is a boolean type. if the arg is present, that's enough.
	public byte getNumberOfArgValues() {
		return 0;
	}
}