import argparser.*;
import argparser.argumentTypes.IntArgument;

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

					addGroup(new ArgumentGroup("another subgroup") {{
						addArgument(new Argument<>("what10", ArgumentType.INTEGER()));
						addArgument(new Argument<>("what11", ArgumentType.INTEGER()));
					}});
				}});

				addGroup(new ArgumentGroup("subgroup2") {{
					exclusive();

					addArgument(new Argument<>("what4", ArgumentType.INTEGER()));
					addArgument(new Argument<>("what5", ArgumentType.INTEGER()));
				}});
			}});
		}};

		final var pArgs = argumentParser.parseArgs("--what10 3 --what5 23");

		System.out.println(pArgs.get("what2").get());
	}
}
