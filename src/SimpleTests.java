import argparser.*;
import argparser.argumentTypes.KeyValuesArgument;
import argparser.utils.ErrorLevel;


public class SimpleTests {
	public static void main(String[] args) {

		var a = new ArgumentParser("SimpleTesting") {{
			setErrorCode(64);
			setMinimumDisplayErrorLevel(ErrorLevel.DEBUG);

			addArgument(new Argument<>("test", ArgumentType.STRING()).onOk(System.out::println).defaultValue("testing"));
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
		a.setOnCorrectCallback(c -> c.addError("Looks like it worked!", ErrorLevel.DEBUG));

		a.parseArgs("subcommand -h[x.23 y.56] jdioawjd");
	}
}
