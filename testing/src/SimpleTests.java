import argparser.*;

import java.io.IOException;

public class SimpleTests {
	public static void main(String[] args) {
		final var argumentParser = new ArgumentParser("Testing", "Some description") {{
			setTupleChars(TupleCharacter.PARENTHESIS);
			setHelpFormatter(new HelpFormatter() {
				@Override
				public void setLayout() {
					super.setLayout();
					this.addToLayout(
						new LayoutItem((c) -> LayoutGenerators.heading("This is a heading")),
						new LayoutItem((c) -> "this has " + c.getArguments().size() + " arguments").indent(3)
					);
				}
			});

			addArgument(new Argument<>("what", new StringJoiner())
				.onOk(t -> System.out.println("wow look a string: '" + t + "'"))
				.positional()
				.obligatory()
			);

			addArgument(new Argument<>("what2", ArgumentType.INTEGER()));

			addSubCommand(new Command("subcommand") {{
				addArgument(new Argument<>("c", ArgumentType.COUNTER()).obligatory());
				addArgument(new Argument<>('s', "more-strings", new StringJoiner()));
				addSubCommand(new Command("another") {{
					addArgument(new Argument<>("ball", new StringJoiner()));
					addArgument(new Argument<>("number", ArgumentType.INTEGER()).positional().obligatory());
				}});
			}});
		}};

		HelpFormatter.debugLayout = true;

//		var pArgs = argumentParser.parseArgs("-fff --test hii subcommand --nose <x.1 y.347 z.43423> another --test 'this is a test' what");
//		final var pArgs = argumentParser.parseArgs("--help");
		final var pArgs = argumentParser.parseArgs("--help");
	}
}
