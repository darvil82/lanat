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
		ap.addArgument(new Argument<>('g', ArgumentType.COUNTER())
			.callback((b) -> System.out.println("I was used " + b + " times")));
		ap.addArgument(new Argument<>('t', "testing", ArgumentType.STRING()).callback(System.out::println));
		ap.addArgument(
			new Argument<>('h', "my-arg", new Multiplier())
				.callback(h -> {
						for (var f : h) {
							System.out.print(f + ", ");
						}
					}
				).obligatory().positional());


		ap.parseArgs("--my-arg 1 6 -g");
	}
}