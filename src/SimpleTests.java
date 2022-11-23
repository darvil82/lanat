import argparser.*;
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
			setMinimumExitErrorLevel(ErrorLevel.ERROR);
			setMinimumDisplayErrorLevel(ErrorLevel.DEBUG);
			setOnCorrectCallback((c) -> System.out.println("Correct!"));
			setOnErrorCallback((c) -> System.out.println("Error!"));

			addArgument(new Argument<>("test", ArgumentType.STRING()));
			addArgument(new Argument<>("what", ArgumentType.FILE()) {{
				setMinimumDisplayErrorLevel(ErrorLevel.INFO);
			}});
			addSubCommand(new Command("subcommand") {{
				setErrorCode(128);
				setOnCorrectCallback((c) -> System.out.println("Correct sub!"));
				setOnErrorCallback((c) -> System.out.println("Error sub!"));

				addArgument(new Argument<>("what", ArgumentType.FILE()));
				addArgument(new Argument<>('h', "hey", ArgumentType.KEY_VALUES(ArgumentType.INTEGER())).onOk(System.out::println).onErr(System.err::println));
			}});
		}};

		a.addError("hello", ErrorLevel.DEBUG);

		a.parseArgs("subcommand --hey [w= test=24] waddwa");
	}
}
