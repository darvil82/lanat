import argparser.*;
import argparser.argumentTypes.KeyValuesArgument;
import argparser.utils.ErrorLevel;

import java.util.HashMap;

public class SimpleTests {
	public static void main(String[] args) {

		final var argumentParser = new ArgumentParser("SimpleTesting") {{
			setErrorCode(64);
			setMinimumDisplayErrorLevel(ErrorLevel.DEBUG);
			setTupleChars(TupleCharacter.ANGLE_BRACKETS);

			addArgument(new Argument<>("f", ArgumentType.COUNTER()).addAliases("c", "b"));
			addArgument(new Argument<>("c", ArgumentType.COUNTER()));

			addGroup(new ArgumentGroup("stuff") {{
				addArgument(new Argument<>("test", ArgumentType.STRING()));
				addArgument(new Argument<>("what", ArgumentType.FILE()));
			}});
			addSubCommand(new Command("subcommand") {{
				setErrorCode(128);

				addArgument(new Argument<>('w', "what", ArgumentType.FILE()));
				addArgument(new Argument<>("nose", new KeyValuesArgument<>(ArgumentType.INTEGER(), '.')));

				addSubCommand(new Command("another") {{
					addArgument(new Argument<>("test", ArgumentType.STRING()).onOk(v ->
						System.out.println("test: " + v)
					));
				}});
			}});
		}};

//		var pArgs = argumentParser.parseArgs("-fff --test hii subcommand --nose <x.1 y.347 z.43423> another --test 'this is a test' what");
		final var pArgs = argumentParser.parseArgs("-fcb");

		System.out.println(pArgs.get("f").get());

		System.out.println(pArgs.<String>get("test").undefined("i am the fallback value for --test"));

		if (pArgs.get("test").defined()) {
			System.out.println("test is defined");
		}

		pArgs.<String>get("test").defined(t -> {
			System.out.println("test is defined");
			System.out.println(t);
		}).undefined(() ->
			System.out.println("test is undefined")
		);

		pArgs.<String>get("subcommand.another.test").defined(System.out::println);
		ParsedArguments.setSeparator("->");
		pArgs.<String>get("subcommand->another->test").defined(System.out::println);

		pArgs.<HashMap<String, Integer>>get("subcommand", "nose").defined(hm -> {
			for (var entry : hm.entrySet()) {
				System.out.println(entry.getKey() + " -> " + entry.getValue());
			}
		});
	}
}
