package argparser;

import argparser.utils.Pair;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public abstract class ArgumentType<T> {
	protected T value;
	/**
	 * This is used for storing errors that occur during parsing. We need to keep track of the index of the token that caused the error.
	 */
	private short tokenIndex = 0;
	/**
	 * This specifies the number of values that this argument received when being parsed.
	 */
	private int receivedValueCount = 0;
	private ArrayList<CustomParseError> errors = new ArrayList<>();

	// Easy to access values. These are methods because we don't want to use the same instance everywhere.
	public static IntArgument INTEGER() {return new IntArgument();}

	public static BooleanArgument BOOLEAN() {return new BooleanArgument();}

	public static CounterArgument COUNTER() {return new CounterArgument();}

	public static StringArgument STRING() {return new StringArgument();}

	public static FileArgument FILE() {return new FileArgument();}

	final void parseArgumentValues(String[] args) {
		this.receivedValueCount = args.length;
		this.parseValues(args);
	}

	public abstract void parseValues(String[] args);
	public void parseValues(String arg) {
		this.parseValues(new String[]{ arg });
	}

	public ArgValueCount getNumberOfArgValues() {
		return ArgValueCount.ONE;
	}

	public String getRepresentation() {
		return this.getClass().getName();
	}

	public T getFinalValue() {
		return this.value;
	}

	protected void addError(String message, int index) {
		this.addError(message, index, ErrorLevel.ERROR);
	}

	protected void addError(String message) {
		this.addError(message, -1);
	}

	protected void addError(String message, int index, ErrorLevel level) {
		if (!this.getNumberOfArgValues().isInRange(index, true) && index != -1) {
			throw new IndexOutOfBoundsException("Index " + index + " is out of range for " + this.getRepresentation());
		}

		this.errors.add(new CustomParseError(
			message,
			this.tokenIndex + (index == -1 ? 0 : Math.min(index + 1, this.receivedValueCount)),
			level
		));
	}

	List<CustomParseError> getErrors() {
		return this.errors;
	}

	void setTokenIndex(short tokenIndex) {
		this.tokenIndex = tokenIndex;
	}
}


class IntArgument extends ArgumentType<Integer> {
	@Override
	public void parseValues(String[] arg) {
		try {
			this.value = Integer.parseInt(arg[0]);
		} catch (NumberFormatException e) {
			this.addError("Invalid integer value: '" + arg[0] + "'.", 0);
		}
	}

	@Override
	public String getRepresentation() {
		return "int";
	}
}


class BooleanArgument extends ArgumentType<Boolean> {
	@Override
	public void parseValues(String[] arg) {
		this.value = true;
	}

	@Override
	public String getRepresentation() {
		return "bool";
	}

	@Override
	// this is a boolean type. if the arg is present, that's enough.
	public ArgValueCount getNumberOfArgValues() {
		return ArgValueCount.NONE;
	}
}


class CounterArgument extends ArgumentType<Short> {
	// prevent nullptr exceptions
	public CounterArgument() {
		this.value = (short)0;
	}

	@Override
	public ArgValueCount getNumberOfArgValues() {
		return ArgValueCount.NONE;
	}

	@Override
	public void parseValues(String[] args) {
		this.value++;
	}
}

class StringArgument extends ArgumentType<String> {
	@Override
	public void parseValues(String[] args) {
		this.value = args[0];
	}
}

class FileArgument extends ArgumentType<FileReader> {
	@Override
	public void parseValues(String[] args) {
		try {
			this.value = new FileReader(args[0]);
		} catch (Exception e) {
			this.addError("File not found: '" + args[0] + "'.", 0);
		}
	}
}