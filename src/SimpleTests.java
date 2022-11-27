import argparser.*;
import argparser.argumentTypes.KeyValuesArgument;
import argparser.utils.ErrorLevel;

public class SimpleTests {
	public static void main(String[] args) {

		var pargs = new ArgumentParser("SimpleTesting") {{
			setErrorCode(64);
			setMinimumDisplayErrorLevel(ErrorLevel.DEBUG);

			addArgument(new Argument<>("f", ArgumentType.COUNTER()));
			addArgument(new Argument<>("test", ArgumentType.STRING()));
			addArgument(new Argument<>("what", ArgumentType.FILE()));
			addSubCommand(new Command("subcommand") {{
				setErrorCode(128);

				addArgument(new Argument<>('w', "what", ArgumentType.FILE()));
				addArgument(new Argument<>("nose", new KeyValuesArgument<>(ArgumentType.INTEGER())));

				addSubCommand(new Command("another") {{
					addArgument(new Argument<>("test", ArgumentType.STRING()));
				}});
			}});
		}}.parseArgs("-fff --test hii subcommand --nose [x=1 y=347 z=43423] another --test 'this is a test'");


		var v = pargs.<String>get("test").undefined("yeah");
		System.out.println(v);

		if (pargs.get("test").defined()) {
			System.out.println("test is defined");
		}

		pargs.<String>get("test").defined(t -> {
			System.out.println("test is defined");
			System.out.println(t);
		}).undefined(() -> {
			System.out.println("test is undefined");
		});

		pargs.<String>get("subcommand.another.test").defined(System.out::println);
		ParsedArguments.separator = "->";
		pargs.<String>get("subcommand->another->test").defined(System.out::println);

		pargs.<String>get("subcommand", "another", "test").defined(System.out::println);
	}
}
