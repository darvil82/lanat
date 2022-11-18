package argparser;

import argparser.utils.UtlString;

import java.util.List;
import java.util.function.Consumer;

public class Argument<Type extends ArgumentType<TInner>, TInner> {
	public static final char[] INVALID_CHARACTERS = {'=', ' '};
	private char prefix = '-';
	private final Type argType;
	private Character name;
	private String alias;
	private Consumer<TInner> callback;
	private short usageCount = 0;
	private boolean obligatory = false, positional = false;
	private TInner defaultValue;
	private Command parentCmd;
	private int errorCode = 1;

	public Argument(Character name, String alias, Type argType) {
		if (name == null && alias == null) {
			throw new IllegalArgumentException("A name or an alias must be specified");
		}
		this.setAlias(alias);
		this.setName(name);
		this.argType = argType;
	}

	public Argument(Character name, Type argType) {
		this(name, null, argType);
	}

	public Argument(String alias, Type argType) {
		this(null, alias, argType);
	}

	@SuppressWarnings("unchecked cast") // we know for sure type returned by BOOLEAN is compatible
	public Argument(Character name) {this(name, null, (Type)ArgumentType.BOOLEAN());}

	/**
	 * Checks if the specified alias is invalid or not
	 *
	 * @return <code>true</code> if the alias is valid
	 */
	private static boolean isValidAlias(String alias) {
		return UtlString.matchCharacters(alias, c -> {
			for (char chr : INVALID_CHARACTERS) {
				if (c == chr) {
					return false;
				}
			}
			return true;
		});
	}

	private static boolean isValidName(char name) {
		for (char invalidChar : Argument.INVALID_CHARACTERS) {
			if (invalidChar == name) return false;
		}
		return true;
	}

	public void setAlias(String alias) {
		if (alias == null) return;
		if (!Argument.isValidAlias(alias)) {
			throw new IllegalArgumentException("invalid alias '" + alias + "'");
		}
		this.alias = alias.replaceAll(String.format("^%s+", this.prefix), "");
	}

	public String getAlias() {
		if (this.alias == null) return this.name.toString();
		return alias;
	}

	public void setName(Character name) {
		if (name == null) return;
		if (!Argument.isValidName(name)) {
			throw new IllegalArgumentException("invalid name '" + name + "'");
		}
		this.name = name;
	}

	public char getPrefix() {
		return prefix;
	}

	/**
	 * Marks the argument as obligatory. This means that this argument should <b>always</b> be used
	 * by the user.
	 */
	public Argument<Type, TInner> obligatory() {
		this.obligatory = true;
		return this;
	}

	/**
	 * Marks the argument as positional. This means that the value of this argument may be specified directly
	 * without indicating the name/alias of this argument. The positional place where it should be placed is
	 * defined by the order of creation of the argument definitions.
	 * <li>Note that an argument marked as positional can still be used by specifying its name/alias.
	 */
	public Argument<Type, TInner> positional() {
		if (this.getNumberOfValues().max == 0) {
			throw new IllegalArgumentException("An argument that does not accept values cannot be positional");
		}
		this.positional = true;
		return this;
	}

	/**
	 * Specify a function that will be called with the value introduced by the user. This function is only
	 * called if the user used the argument, so it will never be called with a default value, for example.
	 */
	public Argument<Type, TInner> callback(Consumer<TInner> cb) {
		this.callback = cb;
		return this;
	}

	/**
	 * Specify the prefix of this argument. By default, this is <code>'-'</code>. If this argument is used in an
	 * argument name list (-abcd), the prefix that will be valid is any against all the arguments specified
	 * in that name list.
	 */
	public Argument<Type, TInner> prefix(char prefix) {
		this.prefix = prefix;
		return this;
	}

	/**
	 * The value that should be used if the user does not specify a value for this argument. If the argument
	 * does not accept values, this value will be ignored.
	 */
	public Argument<Type, TInner> defaultValue(TInner value) {
		this.defaultValue = value;
		return this;
	}

	/**
	 * Specifies the error code that the program should return when this argument fails.
	 * When multiple arguments fail, the program will return the result of the OR bit operation that will be
	 * applied to all other argument results. For example:
	 * <ul>
	 *     <li>Argument 'foo' has a return value of 2. <code>(0b010)</code></li>
	 *     <li>Argument 'bar' has a return value of 5. <code>(0b101)</code></li>
	 * </ul>
	 * Both arguments failed, so in this case the resultant return value would be 7 <code>(0b111)</code>.
	 */
	public Argument<Type, TInner> errorCode(int errorCode) {
		this.errorCode = errorCode;
		return this;
	}

	TInner finishParsing(Command.ParseState parseState) {
		if (this.usageCount == 0) {
			if (this.obligatory) {
				parseState.addError(ParseError.ParseErrorType.OBLIGATORY_ARGUMENT_NOT_USED, this, 0);
				return null;
			}
			return this.defaultValue;
		}

		List<CustomParseError> errors = this.argType.getErrors();

		if (!errors.isEmpty()) {
			errors.forEach(parseState::addError);
			return null;
		}

		return this.argType.getFinalValue();
	}

	public void parseValues(String[] value, short tokenIndex) {
		this.argType.setTokenIndex(tokenIndex);
		this.argType.parseArgumentValues(value);
		this.usageCount++;
	}

	public void parseValues() {
		this.parseValues(new String[0], (short)0);
	}

	public ArgValueCount getNumberOfValues() {
		return this.argType.getNumberOfArgValues();
	}

	public boolean checkMatch(String alias) {
		if (this.alias == null) return false;
		return alias.equals(Character.toString(this.prefix).repeat(2) + this.alias);
	}

	public boolean checkMatch(char name) {
		// getAlias because it has a fallback to return the name if there's no alias.
		// we want to match single-char aliases too
		if (this.name == null) {
			String alias = this.getAlias();
			return alias.length() == 1 && alias.charAt(0) == name;
		}
		return this.name == name;
	}

	public boolean isObligatory() {
		return obligatory;
	}

	public boolean isPositional() {
		return positional;
	}

	void setParentCmd(Command parentCmd) {
		this.parentCmd = parentCmd;
	}

	Command getParentCmd() {
		return parentCmd;
	}

	public boolean equals(Argument<?, ?> obj) {
		// we just want to check if there's a difference between identifiers and both are part of the same command
		return this.getAlias().equals(obj.getAlias()) && this.prefix == obj.prefix && this.parentCmd == obj.parentCmd;
	}

	// we know that this is safe because this argument will always receive its correct type
	@SuppressWarnings("unchecked")
	void invokeCallback(Object value) {
		if (this.callback != null) {
			this.callback.accept((TInner)value);
		}
	}
}