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
//		this.addArgument('h', "help", ArgumentType.BOOLEAN(), t -> System.out.println(this.getHelp()));
		this.addArgument(new Argument<>('h', "help", ArgumentType.BOOLEAN())
				.callback(t -> System.out.println(this.getHelp()))
		);
	}

	public ArgumentParser(String programName) {
		this(programName, "");
	}

	public String getHelp() {
		return "This is the help of the program.";
	}


	public <T extends ArgumentType<TInner>, TInner>
	void addArgument(Argument<T, TInner> argument) {
		arguments.add(argument);
	}

	public void parseArgs(String[] args) {
		for (int x = 0; x < args.length; x++) {
			String arg = args[x];
			for (var argument : this.arguments) {
				if (argument.checkMatch(arg)) {
					byte argValueSkipCount = argument.getNumberOfValues().max;
					argument.parseValues(Arrays.copyOfRange(args, x + 1, x + argValueSkipCount + 1));
					x += argValueSkipCount;

					break;
				}
			}
		}

		this.arguments.forEach(Argument::invokeCallback);
	}
}

