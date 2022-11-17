package argparser;

import argparser.utils.EventHandler;
import argparser.utils.Pair;
import jdk.jfr.Event;

import java.io.FileReader;
import java.lang.reflect.Array;
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
	protected ArrayList<CustomParseError> errors = new ArrayList<>();
	private EventHandler<CustomParseError> errorListeners = new EventHandler<>();



	final void parseArgumentValues(String[] args) {
		this.receivedValueCount = args.length;
		this.parseValues(args);
	}

	public abstract void parseValues(String[] args);

	public void parseValues(String arg) {
		this.parseValues(new String[]{ arg });
	}

	/**
	 * By registering a subtype, this allows you to listen for errors that occurred in this subtype during
	 * parsing. The <code>onSubTypeError</code> method will be called when an error occurs.
	 * @see ArgumentType#onSubTypeError(CustomParseError)
	 */
	protected void registerSubType(ArgumentType<?> subType) {
		subType.errorListeners.addListener(this::onSubTypeError);
	}

	/**
	 * This is called when a subtype of this argument type has an error.
	 * By default, this adds the error to the list of errors, while also adding
	 * the token index of the current type.
	 * @param error The error that occurred in the subtype.
	 */
	protected void onSubTypeError(CustomParseError error) {
		this.addError(error);
	}

	/**
	 * Specifies the number of values that this argument should receive when being parsed.
	 */
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

	protected void addError(String message, int index, ErrorLevel level) {
		if (!this.getNumberOfArgValues().isInRange(index, true)) {
			throw new IndexOutOfBoundsException("Index " + index + " is out of range for " + this.getClass().getName());
		}

		var error = new CustomParseError(
			message,
			this.tokenIndex + Math.min(index + 1, this.receivedValueCount),
			level
		);

		this.errors.add(error);
		this.errorListeners.invoke(error);
	}

	private void addError(CustomParseError error) {
		if (!this.getNumberOfArgValues().isInRange(error.index, true)) {
			throw new IndexOutOfBoundsException("Index " + error.index + " is out of range for " + this.getClass().getName());
		}

		error.index = this.tokenIndex + Math.min(error.index + 1, this.receivedValueCount);

		this.errors.add(error);
		this.errorListeners.invoke(error);
	}

	protected void addErrors(ArgumentType<?> errors) {
		errors.getErrors().forEach(e -> {
			e.index += this.tokenIndex;
			this.errors.add(e);
		});
	}

	List<CustomParseError> getErrors() {
		return this.errors;
	}

	public boolean hasErrors() {
		return !this.errors.isEmpty();
	}

	void setTokenIndex(short tokenIndex) {
		this.tokenIndex = tokenIndex;
	}

	protected short getTokenIndex() {
		return tokenIndex;
	}


	// Easy to access values. These are methods because we don't want to use the same instance everywhere.
	public static IntArgument INTEGER() {return new IntArgument();}

	public static BooleanArgument BOOLEAN() {return new BooleanArgument();}

	public static CounterArgument COUNTER() {return new CounterArgument();}

	public static StringArgument STRING() {return new StringArgument();}

	public static FileArgument FILE() {return new FileArgument();}
	public static <T extends ArgumentType<Ts>, Ts> KeyValuesArgument<T, Ts>
	KEY_VALUES(T valueType) {return new KeyValuesArgument<>(valueType);}
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

class KeyValuesArgument<T extends ArgumentType<Ts>, Ts> extends ArgumentType<List<Pair<String, Ts>>> {
	private ArgumentType<Ts> valueType;
	private int keyIndex = 0;

	public KeyValuesArgument(T type) {
		this.valueType = type;
		this.registerSubType(type);
	}

	@Override
	protected void onSubTypeError(CustomParseError error) {
		error.index += this.keyIndex;
		super.onSubTypeError(error);
	}

	@Override
	public ArgValueCount getNumberOfArgValues() {
		return new ArgValueCount(1, -1);
	}

	@Override
	public void parseValues(String[] args) {
		this.value = new ArrayList<>();

		for (; this.keyIndex < args.length; this.keyIndex++) {
			String arg = args[this.keyIndex];
			String[] split = arg.split("=");

			if (split.length != 2) {
				this.addError("Invalid key-value pair: '" + arg + "'.", this.keyIndex);
				continue;
			}

			valueType.parseValues(split[1]);

			this.value.add(new Pair<>(split[0], valueType.getFinalValue()));
		}
	}
}