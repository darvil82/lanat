import argparser.ArgValueCount;
import argparser.Argument;
import argparser.ArgumentParser;
import argparser.ArgumentType;

import java.util.Arrays;

class Multiplier extends ArgumentType<Float[]> {
	@Override
	public ArgValueCount getNumberOfArgValues() {
		return new ArgValueCount(1, 3);
	}

	@Override
	public void parseArgValues(String[] args) {
		this.value = Arrays.stream(args).map(s -> Float.parseFloat(s) * 2).toArray(Float[]::new);
	}
}

public class Testing {
	public static void main(String[] args) throws Exception {
		var ap = new ArgumentParser("Testing");
		ap.addArgument(
			new Argument<>("string", ArgumentType.STRING())
				.callback(t -> System.out.println("wow look a string: " + t))
				.positional()
		);
		ap.addArgument(new Argument<>("arg", ArgumentType.BOOLEAN()).callback(System.out::println));
		ap.addArgument(new Argument<>("c", ArgumentType.COUNTER()).callback(t -> System.out.println(t)));
		ap.parseArgs("['test test' 'another one'] --arg");
	}
}