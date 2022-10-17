package argparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

public class ArgumentParser {
	protected final String programName, description;
	protected ArrayList<Argument<?, ?>> arguments = new ArrayList<>();


	public ArgumentParser(String programName, String description) {
		this.programName = programName;
		this.description = description;
		this.addArgument('h', "help", ArgumentType.BOOLEAN(), t -> System.out.println(this.getHelp()));
	}

	public ArgumentParser(String programName) {
		this(programName, "");
	}

	public String getHelp() {
		return "This is the help of the program.";
	}


	public <T extends ArgumentType<TInner>, TInner>
	void addArgument(Character name, String alias, T argType, Consumer<TInner> callback) {
		arguments.add(new Argument<>(name, alias, argType, callback));
	}

	public <T extends ArgumentType<TInner>, TInner>
	void addArgument(Character name, T argType, Consumer<TInner> callback) {
		this.addArgument(name, null, argType, callback);
	}

	public <T extends ArgumentType<TInner>, TInner>
	void addArgument(String alias, T argType, Consumer<TInner> callback) {
		this.addArgument(null, alias, argType, callback);
	}

	public void addArgument(Character name, Consumer<Boolean> callback) {
		this.addArgument(name, null, ArgumentType.BOOLEAN(), callback);
	}

	public void parseArgs(String[] args) {
		for (int x = 0;x < args.length; x++) {
			String arg = args[x];
			for (var argument : this.arguments) {
				if (argument.checkMatch(arg)) {
					byte argValueSkipCount = argument.getNumberOfValues();
					argument.parseValues(Arrays.copyOfRange(args, x + 1, x + argValueSkipCount + 1));
					x += argValueSkipCount;

					break;
				}
			}
		}

		this.arguments.forEach(Argument::invokeCallback);
	}
}

class Argument<TInner, Type extends ArgumentType<TInner>> {
	private final char prefix = '-';
	private final Type argType;
	private Character name;
	private String alias;
	private Consumer<TInner> callback;
	private byte usageCount = 0;

	public Argument(Character name, String alias, Type argType, Consumer<TInner> callback) {
		this.setAlias(alias);
		this.argType = argType;
		this.name = name;
		this.callback = callback;
	}

	public void setAlias(String alias) {
		if (alias == null) return;
		this.alias = alias.replaceAll('^' + Character.toString(this.prefix), "");
	}

	public void invokeCallback() {
		if (this.usageCount == 0) return;
		this.callback.accept(this.argType.getFinalValue());
	}

	public void parseValues(String[] value) {
		this.argType.parseArgValues(value);
		this.usageCount++;
	}

	public byte getNumberOfValues() {
		return this.argType.getNumberOfArgValues();
	}

	public boolean checkMatch(String alias) {
		return alias.equals(Character.toString(this.prefix).repeat(2) + this.alias);
	}

	public boolean checkMatch(char name) {
		return name == this.name;
	}
}