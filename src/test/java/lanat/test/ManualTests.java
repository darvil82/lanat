package lanat.test;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.ArgumentType;
import lanat.Command;
import lanat.argumentTypes.Parseable;
import lanat.utils.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

public final class ManualTests {
	@Test
	public void main() {
//		HelpFormatter.lineWrapMax = 110;
//		HelpFormatter.debugLayout = true;

		enum TestEnum {
			ONE, TWO, THREE
		}

		var parser = new TestingParser("Testing", "<color=yellow><format=bold,u,italic>"
			+ "hello<color=white><format=!u>, the argument <link=args.group-arg> is formatted! "
			+ "This is its type description: <desc=args.group-arg.type>"
		) {{
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
				this.addArgument(Argument.create("group-arg2", ArgumentType.ENUM(TestEnum.ONE))
					.onOk(value -> System.out.println("2: " + value))
					.description("<desc=!.type>")
				);
			}});

			this.addSubCommand(new Command("hello", "Some description for the command") {{
				this.addNames("hi", "hey");
				this.addArgument(Argument.create("world", ArgumentType.INTEGER_RANGE(5, 10))
					.onOk(value -> System.out.println("ok: " + value))
				);
			}});

			this.addSubCommand(new Command("goodbye", "Some description for this other command") {{
				this.addNames("bye", "cya");
				this.addArgument(Argument.create("world", new StringJoiner())
					.onOk(value -> System.out.println("ok: " + value))
				);
			}});
		}};

		parser.parseArgs("--help");
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
}