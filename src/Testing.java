import argparser.ArgValueCount;
import argparser.Argument;
import argparser.ArgumentParser;
import argparser.ArgumentType;

import java.util.Arrays;

class Multiplier extends ArgumentType<Float[]> {
	@Override
	public ArgValueCount getNumberOfArgValues() {
		return new ArgValueCount(3);
	}

	@Override
	public void parseArgValues(String[] args) {
		this.value = Arrays.stream(args).map(s -> Float.parseFloat(s) * 2).toArray(Float[]::new);
	}
}

public class Testing {
	public static void main(String[] args) {
		var argparser = new ArgumentParser("My Program");
		argparser.addArgument(new Argument<>('g').obligatory());
		argparser.addArgument(new Argument<>('t', "testing", ArgumentType.INTEGER()));
		argparser.addArgument(
				new Argument<>('h', "my-arg", new Multiplier())
						.callback(h -> {
									for (var f : h) {
										System.out.print(f + ", ");
									}
								}
						));

		argparser.parseArgs(new String[]{"--testing", "234", "13981", "--my-arg", "5", "10", "2.5"});
	}
}