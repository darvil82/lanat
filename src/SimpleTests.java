import argparser.Argument;
import argparser.ArgumentParser;
import argparser.ArgumentType;
import argparser.Command;
import argparser.utils.UtlString;

import java.io.BufferedReader;
import java.io.IOException;

public class SimpleTests {
	public static void main(String[] args) {
		var argParser = new ArgumentParser("SimpleTesting") {{
			addArgument(new Argument<>("what", ArgumentType.FILE())
				.positional()
				.obligatory()
			);
		}};
		argParser.parseArgs("--what D:\\\\program files\\\\Steam\\\\steamapps\\\\common\\\\Portal\\ 2\\ Community\\ Edition\\\\hammer\\\\cfg");
	}
}
