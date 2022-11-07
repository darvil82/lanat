import argparser.*;
import argparser.displayFormatter.TextFormatter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;


class StringJoiner extends ArgumentType<String> {
	@Override
	public ArgValueCount getNumberOfArgValues() {
		return new ArgValueCount(1, 3);
	}

	@Override
	public void parseArgValues(String[] args) {
		this.value = "(" + String.join("), (", args) + ")";
	}
}

public class GlobalTests {
	private ArgumentParser parser;

	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	private final PrintStream originalErr = System.err;

	@AfterEach
	public void restoreStreams() {
		System.setErr(originalErr);
	}

	@BeforeEach
	public void setUp() {
		System.setErr(new PrintStream(errContent));

		this.parser = new ArgumentParser("Testing") {{
			addArgument(new Argument<>("what", new StringJoiner())
				.callback(t -> System.out.println("wow look a string: '" + t + "'"))
				.positional()
				.obligatory()
				.errorCode(0x01)
			);
			addArgument(new Argument<>("a", ArgumentType.BOOLEAN()));
			addSubCommand(new Command("subcommand") {{
				addArgument(new Argument<>("c", ArgumentType.COUNTER()));
				addArgument(new Argument<>('s', "more-strings", new StringJoiner()));
				addSubCommand(new Command("another") {{
					addArgument(new Argument<>("ball", new StringJoiner()));
					addArgument(new Argument<>("number", ArgumentType.INTEGER()).positional().obligatory());
				}});
			}});
		}};
	}

	private void assertErrorOutput(String expected) {
		// remove all the decorations to not make the tests a pain to write
		assertEquals(
			expected,
			TextFormatter.removeSequences(errContent.toString())
				.replaceAll(" *[│─└] ?", "")
				.trim()
		);
		System.out.printf("Test error output:\n%s", errContent);
	}

	@Test
	public void testFirstObligatoryArgument() {
		this.parser.parseArgs("subcommand");
		assertErrorOutput("""
			<- subcommand
			Obligatory argument 'what' not used.""");
	}

	@Test
	public void testLastObligatoryArgument() {
		this.parser.parseArgs("foo subcommand another");
		assertErrorOutput("""
			foo subcommand another <-
			Obligatory argument 'number' not used.""");
	}

	@Test
	public void testExceedValueCount() {
		this.parser.parseArgs("--what [1 2 3 4 5 6 7 8 9 10]");
		assertErrorOutput("""
			--what [ 1 2 3 4 5 6 7 8 9 10 ]
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 10.""");
	}

	@Test
	public void testMissingValue() {
		this.parser.parseArgs("--what []");
		assertErrorOutput("""
			--what [ ]
			Incorrect number of values for argument 'what'.
			Expected from 1 to 3 values, but got 0.""");
	}
}