import argparser.*;

public class SimpleTests {
	public static void main(String[] args) {
		final var argumentParser = new ArgumentParser("Testing", "Some description") {{
			addArgument(new Argument<>("simple test", ArgumentType.INTEGER()));

			addSubCommand(new Command("command") {{
				addArgument(new Argument<>("what2", ArgumentType.INTEGER()));
			}});

			addGroup(new ArgumentGroup("some group") {{
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

		final var pArgs = argumentParser.parseArgs("--what2 23 --what5 89 command --what2 2 -- this are some values for other sthuff");
		System.out.println(pArgs.getForwardValue());
	}
}
