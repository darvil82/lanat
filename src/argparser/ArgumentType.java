package argparser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public abstract class ArgumentType<T> {
	protected T value;
	/**
	 * This is used for storing errors that occur during parsing. We need to keep track of the index of the token that caused the error.
	 */
	private short tokenIndex = 0;
	private ArrayList<CustomParseError> errors = new ArrayList<>();

	// Easy to access values. These are methods because we don't want to use the same instance everywhere.
	public static IntArgument INTEGER() {return new IntArgument();}

	public static BooleanArgument BOOLEAN() {return new BooleanArgument();}

	public static CounterArgument COUNTER() {return new CounterArgument();}

	public static StringArgument STRING() {return new StringArgument();}

	public static FileArgument FILE() {return new FileArgument();}


	public abstract void parseArgValues(String[] args);

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
		this.errors.add(new CustomParseError(message, this.tokenIndex + (index == -1 ? 0 : index + 1), level));
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
	public void parseArgValues(String[] arg) {
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
	public void parseArgValues(String[] args) {
		this.value++;
	}
}

class StringArgument extends ArgumentType<String> {
	@Override
	public void parseArgValues(String[] args) {
		this.value = args[0];
	}
}

class FileArgument extends ArgumentType<FileReader> {
	@Override
	public void parseArgValues(String[] args) {
		try {
			this.value = new FileReader(args[0]);
		} catch (Exception e) {
			this.addError("File not found: '" + args[0] + "'.", 0);
		}
	}
}