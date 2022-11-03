package argparser;

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
		for (char invalidChar : Argument.INVALID_CHARACTERS) {
			if (alias.contains(Character.toString(invalidChar))) {
				return false;
			}
		}
		return true;
	}

	private static boolean isValidName(char name) {
		for (char invalidChar : Argument.INVALID_CHARACTERS) {
			if (invalidChar == name) return false;
		}
		return true;
	}

	public void setAlias(String alias) {
		if (alias == null || !Argument.isValidAlias(alias)) return;
		this.alias = alias.replaceAll(String.format("^%s+", this.prefix), "");
	}

	public String getAlias() {
		if (this.alias == null) return this.name.toString();
		return alias;
	}

	public void setName(Character name) {
		if (name == null || !Argument.isValidName(name)) return;
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

	ParseResult<TInner> finishParsing() {
		if (this.usageCount == 0) {
			return this.isObligatory()
				? ParseResult.ERROR(ParseErrorType.ObligatoryArgumentNotUsed)
				: ParseResult.CORRECT(this.defaultValue);
		}

		var finalValue = this.argType.getFinalValue();

		if (this.callback != null) this.callback.accept(finalValue);

		return ParseResult.CORRECT(finalValue);
	}

	public void parseValues(String[] value) {
		this.argType.parseArgValues(value);
		this.usageCount++;
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
			var alias = this.getAlias();
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

	public boolean equals(Argument<?, ?> obj) {
		// we just want to check if there's a difference between identifiers
		return this.getAlias().equals(obj.getAlias()) && this.prefix == obj.prefix;
	}
}