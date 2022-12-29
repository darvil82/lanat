import argparser.*;

import java.util.Arrays;

public class SimpleTests {
	public static void main(String[] args) {
		final var argumentParser = new ArgumentParser("Testing", "Some description") {{
			addGroup(new ArgumentGroup("some group") {{
				exclusive();

				addGroup(new ArgumentGroup("subgroup") {{
					exclusive();

					addArgument(new Argument<>("what2", ArgumentType.INTEGER()));
					addArgument(new Argument<>("what3", ArgumentType.INTEGER()));
				}});

				addGroup(new ArgumentGroup("subgroup2") {{
					exclusive();

					addArgument(new Argument<>("what4", ArgumentType.INTEGER()));
					addArgument(new Argument<>("what5", ArgumentType.INTEGER()));
				}});
			}});
		}};

		final var pArgs = argumentParser.parseArgs("--what2 3 --what3 5 --what4 7");

		System.out.println(pArgs.get("what2").get());
	}
}
