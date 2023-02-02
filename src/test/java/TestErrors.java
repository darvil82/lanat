import argparser.Argument;
import argparser.ArgumentType;
import argparser.Command;
import argparser.NamedWithDescription;
import argparser.utils.ErrorCallbacks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TestErrors extends UnitTests {
	private final HashMap<String, Object> correct = new HashMap<>();
	private final HashMap<String, Object> invalid = new HashMap<>();

	@BeforeEach
	public void clear() {
		this.correct.clear();
		this.invalid.clear();
	}

	private <T extends ErrorCallbacks<?, ?> & NamedWithDescription> T addCallbacks(T obj) {
		obj.setOnCorrectCallback((v) -> this.correct.put(obj.getName(), v));
		obj.setOnErrorCallback((a) -> this.invalid.put(obj.getName(), a));
		return obj;
	}

	private void assertOk(String name, Object correct) {
		assertEquals(this.correct.get(name), correct);
		assertNull(this.invalid.get(name));
	}

	private void assertErr(String name) {
		assertNull(this.correct.get(name));
		assertNotNull(this.invalid.get(name));
	}

	@Override
	protected void assertNotPresent(String name) {
		assertNull(this.correct.get(name));
		assertNull(this.invalid.get(name));
	}

	@Override
	public void setParser() {
		this.parser = addCallbacks(new TestingParser("TestCallbacks") {{
			setErrorCode(5);

			addArgument(addCallbacks(new Argument<>("bool-arg", ArgumentType.BOOLEAN())));
			addArgument(addCallbacks(new Argument<>("int-arg", ArgumentType.INTEGER())));
			addArgument(addCallbacks(new Argument<>("counter", ArgumentType.COUNTER())));
			addArgument(addCallbacks(new Argument<>("will-work", ArgumentType.FLOAT())));

			addSubCommand(addCallbacks(new Command("sub") {{
				addArgument(addCallbacks(new Argument<>("will-fail", ArgumentType.FLOAT())));
				setErrorCode(2);
			}}));
		}});
	}

	@Test
	public void testArgumentCallbacks() {
		this.parser.parseArgs("--bool-arg --int-arg foo --will-work 55.0 sub --will-fail bar");
		assertOk("bool-arg", true);
		assertErr("int-arg");
		assertNotPresent("counter");
		assertOk("will-work", 55.0f);

		assertErr("will-fail");
	}

	@Test
	public void testCommandCallbacks() {
		this.parser.parseArgs("sub --will-fail bar");
		assertErr("will-fail");
		assertErr(this.parser.getName());
	}

	@Test
	public void testCommandsErrorCode() {
		this.parser.parseArgs("sub --will-fail bar");
		assertEquals(this.parser.getErrorCode(), 7);
	}
}
