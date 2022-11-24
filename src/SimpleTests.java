import argparser.*;
import argparser.argumentTypes.KeyValuesArgument;
import argparser.utils.ErrorLevel;

class Ball extends ArgumentType<Integer> {
	@Override
	public ArgValueCount getNumberOfArgValues() {
		return new ArgValueCount(0, 2);
	}

	@Override
	public void parseValues(String[] args) {
		this.addError("This is a test error", 0, ErrorLevel.INFO);
	}
}


public class SimpleTests {
	public static void main(String[] args) {

		var a = new ArgumentParser("SimpleTesting") {{
			setErrorCode(64);
			setMinimumDisplayErrorLevel(ErrorLevel.DEBUG);

			addArgument(new Argument<>("test", ArgumentType.STRING()));
			addArgument(new Argument<>("what", ArgumentType.FILE()));
			addSubCommand(new Command("subcommand") {{
				setErrorCode(128);

				addArgument(new Argument<>('w', "what", ArgumentType.FILE()));
				addArgument(new Argument<>('h', "hey", KeyValuesArgument.create(ArgumentType.STRING(), '.'))
					.onOk(System.out::println)
				);
			}});
		}};

		a.setOnErrorCallback(c -> c.addError("Looks like it failed!", ErrorLevel.DEBUG));

		a.parseArgs("subcommand --hey=[x.23 y.56] --what=test");
	}
}
