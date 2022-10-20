package argparser;

import java.io.File;

public abstract class ArgumentType<T> {
	protected T value;

	// Easy to access values. These are methods because we don't want to use the same instance everywhere.
	public static IntArgument INTEGER() {return new IntArgument();}

	public static BooleanArgument BOOLEAN() {return new BooleanArgument();}

	public static CounterArgument COUNTER() {return new CounterArgument();}

	public static StringArgument STRING() {return new StringArgument();}

	public static FileArgument FILE() {return new FileArgument();}

	public abstract void parseArgValues(String[] args);

	public ArgValueCount getNumberOfArgValues() {
		return new ArgValueCount(1);
	}

	public String getRepresentation() {
		return this.getClass().getName();
	}

	public T getFinalValue() {
		return this.value;
	}
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
	public ArgValueCount getNumberOfArgValues() {
		return new ArgValueCount(0);
	}
}


class CounterArgument extends ArgumentType<Short> {
	// prevent nullptr exceptions
	public CounterArgument() {
		this.value = (short)0;
	}

	@Override
	public ArgValueCount getNumberOfArgValues() {
		return ArgValueCount.ANY;
	}

	@Override
	public void parseArgValues(String[] args) {
		this.value++;
	}
}

class StringArgument extends ArgumentType<String> {
	@Override
	public ArgValueCount getNumberOfArgValues() {
		return new ArgValueCount(1);
	}

	@Override
	public void parseArgValues(String[] args) {
		this.value = args[0];
	}
}

class FileArgument extends ArgumentType<File> {
	@Override
	public ArgValueCount getNumberOfArgValues() {
		return new ArgValueCount(1);
	}

	@Override
	public void parseArgValues(String[] args) {
		this.value = new File(args[0]);
	}
}