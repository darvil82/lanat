import argparser.*;
import argparser.displayFormatter.Color;
import argparser.displayFormatter.FormatOption;
import argparser.displayFormatter.TextFormatter;
import argparser.utils.UtlString;

import java.io.BufferedReader;
import java.io.IOException;

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
		var argParser = new ArgumentParser("SimpleTesting") {{
			addArgument(new Argument<>("what", ArgumentType.FILE()));
			addArgument(new Argument<>("w", ArgumentType.PAIR(ArgumentType.INTEGER(), ArgumentType.FILE())).callback(t -> {
				System.out.printf("id: '%d', file to change: '%s'%n", t.first(), t.second());
			}));
			addSubCommand(new Command("subcommand") {{
				addArgument(new Argument<>("what", ArgumentType.FILE()));
				addArgument(new Argument<>("hey", new Ball()));
			}});
		}};
		argParser.parseArgs("-w hola wtf.txt subcommand --hey --what D:\\\\program files\\\\Steam\\\\steamapps\\\\common\\\\Portal\\ 2\\\\gameinfo.txt");
	}
}
