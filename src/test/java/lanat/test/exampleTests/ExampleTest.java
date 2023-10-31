package lanat.test.exampleTests;

import lanat.*;
import lanat.argumentTypes.NumberRangeArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

public final class ExampleTest {
	@Test
	public void main() {
		new ArgumentParser("my-program") {{
			this.addArgument(Argument.create(new Example1Type(), "user"));
			this.addArgument(Argument.create(new NumberRangeArgumentType<>(0.0, 15.23), "number"));
		}}.parse(CLInput.from("--user hello --number 42.1"))
			.getErrors()
			.forEach(System.out::println);
	}

	public static class Example1Type extends ArgumentType<String> {
		@Override
		public @Nullable String parseValues(@NotNull String... args) {
			this.addError("Could not find the user '" + args[0] + "' in the database.", ErrorLevel.WARNING);
			return args[0];
		}
	}
}