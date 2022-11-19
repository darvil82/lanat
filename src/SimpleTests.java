import argparser.*;

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

		new ArgumentParser("SimpleTesting") {{
			addArgument(new Argument<>("test", ArgumentType.STRING()));
			addArgument(new Argument<>("what", ArgumentType.FILE()));
			addSubCommand(new Command("subcommand") {{
				addArgument(new Argument<>("what", ArgumentType.FILE()));
				addArgument(new Argument<>("hey", ArgumentType.KEY_VALUES(ArgumentType.INTEGER())).callback(System.out::println));
			}});
		}}.parseArgs("subcommand --hey [h=12 'test=   a'] files\\\\Steam\\\\steamapps\\\\common\\\\Portal\\ 2\\\\gameinfo.txt");
	}
}
