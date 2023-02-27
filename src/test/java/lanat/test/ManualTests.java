package lanat.test;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.ArgumentType;
import lanat.Command;
import lanat.argumentTypes.Parseable;
import lanat.helpRepresentation.HelpFormatter;
import lanat.utils.Range;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

public final class ManualTests {
	@Test
	public void main() {
		HelpFormatter.lineWrapMax = 110;
		HelpFormatter.debugLayout = true;

		new TestingParser("Testing", "description for main parser") {{
			this.addArgument(Argument.create("testing", ArgumentType.FROM_PARSEABLE(new TestClass()))
				.description("some description")
				.onOk(value -> System.out.println("ok: " + value))
			);

			this.addGroup(new ArgumentGroup("group") {{
				this.exclusive();
				this.addArgument(Argument.create("group-arg", ArgumentType.STRING())
					.onOk(value -> System.out.println("1: " + value))
					.description("some description")
				);
				this.addArgument(Argument.create("group-arg2", ArgumentType.BOOLEAN()).onOk(value -> System.out.println("2: " + value)));
			}});

			this.addSubCommand(new Command("hello", "Some description for the command") {{
				this.addNames("hi", "hey");
				this.addArgument(Argument.create("world", ArgumentType.INTEGER_RANGE(5, 10))
					.description("a range between 5 and 10")
					.onOk(value -> System.out.println("ok: " + value))
				);
			}});

			this.addSubCommand(new Command("goodbye", "Some description for this other command") {{
				this.addNames("bye", "cya");
				this.addArgument(Argument.create("world", ArgumentType.INTEGER_RANGE(5, 10))
					.description("a range between 5 and 10")
					.onOk(value -> System.out.println("ok: " + value))
				);
			}});
		}}.parseArgsExpectErrorPrint("--help");
	}
}


class TestClass implements Parseable<Integer> {

	@Override
	public @NotNull Range getRequiredArgValueCount() {
		return Range.ONE;
	}

	@Override
	public @Nullable Integer parseValues(@NotNull String @NotNull [] args) {
		return Integer.parseInt(args[0]);
	}

	@Override
	public @Nullable TextFormatter getRepresentation() {
		return null;
	}
}