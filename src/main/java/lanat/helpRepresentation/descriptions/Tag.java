package lanat.helpRepresentation.descriptions;

import lanat.*;
import lanat.utils.UtlReflection;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public abstract class Tag {
	private static final Hashtable<String, Tag> registeredTags = new Hashtable<>();
	private static boolean initializedTags;


	protected abstract <T extends CommandUser & NamedWithDescription>
	@NotNull String parse(@NotNull String value, @NotNull T user);


	public static void initTags() {
		if (Tag.initializedTags) return;

		Tag.registerTag("link", new LinkTag());

		Tag.initializedTags = true;
	}

	protected static <T extends Tag> void registerTag(@NotNull String name, @NotNull T tag) {
		Tag.registeredTags.put(name, tag);
	}

	static <T extends CommandUser & NamedWithDescription>
	@NotNull String parseTagValue(@NotNull String tagName, @NotNull String value, @NotNull T user) {
		var tag = Tag.registeredTags.get(tagName);
		if (tag == null) throw new IllegalArgumentException("tag " + UtlString.surround(tagName) + " does not exist");
		return tag.parse(value.trim(), user);
	}

	public static class RouteParser<T extends CommandUser & NamedWithDescription> {
		private NamedWithDescription current;
		private final String[] route;
		private int index;

		private RouteParser(T runner, String route) {
			String[] r = route.split("\\.");

			if (r[0].equals("!")) {
				this.current = runner;
				this.route = Arrays.copyOfRange(r, 1, r.length);
			} else {
				this.current = runner instanceof Command cmd ? cmd : runner.getParentCommand();
				this.route = r;
			}
		}


		public static <T extends CommandUser & NamedWithDescription>
		NamedWithDescription parseRoute(T runner, String route) {
			return new RouteParser<>(runner, route).parse();
		}

		private NamedWithDescription parse() {
			for (this.index = 0; this.index < this.route.length; this.index++) {
				final String token = this.route[this.index];

				if (token.equals("args") && this.current instanceof ArgumentAdder argsContainer) {
					this.test(argsContainer.getArguments());
				} else if (token.equals("groups") && this.current instanceof ArgumentGroupAdder groupsContainer) {
					groupsContainer.getSubGroups().stream()
						.filter(a -> a.getName().equals(this.route[++this.index]))
						.findFirst()
						.ifPresent(argumentGroup -> this.current = argumentGroup);
				} else if (token.equals("cmds") && this.current instanceof Command cmdsContainer) {
					this.test(cmdsContainer.getSubCommands());
				} else if (token.equals("type") && this.current instanceof Argument<?,?> arg) {
					this.current = arg.argType;
				} else {
					throw new RuntimeException("Unknown route value " + UtlString.surround(token));
				}
			}

			return this.current;
		}

		private void test(List<? extends MultipleNamesAndDescription<?>> thing) {
			var name = this.route[++this.index];
			var res = thing.stream().filter(a -> a.hasName(name)).findFirst();
			if (res.isEmpty())
				throw new RuntimeException("Element " + name + " is not present in " + UtlReflection.getSimpleName(this.current.getClass()) + ' ' + this.current.getName());
			else
				this.current = res.get();
		}
	}
}
