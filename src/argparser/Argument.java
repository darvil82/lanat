package argparser;

import java.util.function.Consumer;

public class Argument<Type extends ArgumentType<TInner>, TInner> {
	public final char prefix = '-';
	private final Type argType;
	private Character name;
	private String alias;
	private Consumer<TInner> callback;
	private byte usageCount = 0;
	private boolean obligatory = false;
	public static final char[] INVALID_CHARACTERS = {'=', ' '};

	public Argument(Character name, String alias, Type argType) {
		this.setAlias(alias);
		this.argType = argType;
		this.name = name;
	}

	public Argument(Character name, Type argType) {
		this(name, null, argType);
	}
	public Argument(String alias, Type argType) {
		this(null, alias, argType);
	}
	@SuppressWarnings("unchecked cast") // we know for sure type returned by BOOLEAN is compatible
	public Argument(Character name) {this(name, null, (Type)ArgumentType.BOOLEAN()); }

	public void setAlias(String alias) {
		if (alias == null || !Argument.isInvalidAlias(alias)) return;
		this.alias = alias.replaceAll('^' + Character.toString(this.prefix) + "+", "");
	}

	public Argument<?, ?> obligatory() {
		this.obligatory = true;
		return this;
	}

	public Argument<?, ?> callback(Consumer<TInner> cb) {
		this.callback = cb;
		return this;
	}

	public void invokeCallback() {
		if (this.usageCount == 0 || this.callback == null) return;
		this.callback.accept(this.argType.getFinalValue());
	}

	public void parseValues(String[] value) {
		this.argType.parseArgValues(value);
		this.usageCount++;
	}

	public ArgValueCount getNumberOfValues() {
		return this.argType.getNumberOfArgValues();
	}

	public boolean checkMatch(String alias) {
		return alias.equals(Character.toString(this.prefix).repeat(2) + this.alias);
	}

	public boolean checkMatch(char name) {
		return name == this.name;
	}

	/**
	 * Checks if the specified alias is invalid or not
	 * @return <code>true</code> if the alias is valid
	 */
	private static boolean isInvalidAlias(String alias) {
		for (char invalidChar : Argument.INVALID_CHARACTERS) {
			if (alias.contains(Character.toString(invalidChar))) {
				return false;
			}
		}
		return true;
	}
}