import argparser.ArgValueCount;
import argparser.Argument;
import argparser.ArgumentParser;
import argparser.ArgumentType;

class StringJoiner extends ArgumentType<String> {
	@Override
	public ArgValueCount getNumberOfArgValues() {
		return new ArgValueCount(0, 3);
	}

	@Override
	public void parseArgValues(String[] args) {
		this.value = "(" + String.join("), (", args) + ")";
	}
}

public class Testing {
	public static void main(String[] args) throws Exception {
		var ap = new ArgumentParser("Testing");
		ap.addArgument(
			new Argument<>("string", new StringJoiner())
				.callback(t -> System.out.println("wow look a string: '" + t + "'"))
				.positional()
		);
		ap.addArgument(new Argument<>("arg", ArgumentType.BOOLEAN()).callback(System.out::println));
		ap.addArgument(new Argument<>("c", ArgumentType.COUNTER()).callback(System.out::println));
		ap.parseArgs("['test test' 'another one'] --arg".split(" "));
	}
}