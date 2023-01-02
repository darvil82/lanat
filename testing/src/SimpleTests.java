import argparser.*;

public class SimpleTests {
	public static void main(String[] args) {
//		HelpFormatter.lineWrapMax = 1000;

		final var argumentParser = new ArgumentParser("Testing", "Some description") {{
			addArgument(new Argument<>("simple test", ArgumentType.INTEGER()));

			addSubCommand(new Command("command") {{
				addArgument(new Argument<>("what2", ArgumentType.INTEGER()));
			}});

			addGroup(new ArgumentGroup("some group") {{
				addGroup(new ArgumentGroup("subgroup") {{
					exclusive();

					addGroup(new ArgumentGroup("yup another one") {{
						exclusive();

						addArgument(new Argument<>("what20", ArgumentType.INTEGER()));
						addArgument(new Argument<>("what21", ArgumentType.INTEGER()));
						addGroup(new ArgumentGroup("bla") {{
							exclusive();
						}});
					}});

					addGroup(new ArgumentGroup("yeah quite a few") {{
						exclusive();

						addArgument(new Argument<>("what25", ArgumentType.INTEGER()));
						addArgument(new Argument<>("what26", ArgumentType.INTEGER()));
					}});
				}});

				addGroup(new ArgumentGroup("subgroup2") {{
					exclusive();

					addArgument(new Argument<>("what4", ArgumentType.INTEGER()));
					addArgument(new Argument<>("what5", ArgumentType.INTEGER()));
				}});
			}});
		}};


		final var pArgs = argumentParser.parseArgs("--help");
		System.out.println(pArgs.getForwardValue());
	}
}
