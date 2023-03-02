package lanat.helpRepresentation.descriptions;

import lanat.*;
import lanat.helpRepresentation.descriptions.exceptions.InvalidRouteException;
import lanat.utils.UtlReflection;

import java.util.Arrays;
import java.util.List;

public class RouteParser<T extends CommandUser & NamedWithDescription> {
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
				this.setCurrent(argsContainer.getArguments());
			} else if (token.equals("groups") && this.current instanceof ArgumentGroupAdder groupsContainer) {
				groupsContainer.getSubGroups().stream()
					.filter(a -> a.getName().equals(this.route[++this.index]))
					.findFirst()
					.ifPresent(argumentGroup -> this.current = argumentGroup);
			} else if (token.equals("cmds") && this.current instanceof Command cmdsContainer) {
				this.setCurrent(cmdsContainer.getSubCommands());
			} else if (token.equals("type") && this.current instanceof Argument<?, ?> arg) {
				this.current = arg.argType;
			} else {
				throw new InvalidRouteException(token, this.current);
			}
		}

		return this.current;
	}

	private void setCurrent(List<? extends MultipleNamesAndDescription<?>> thing) {
		var name = this.route[++this.index];
		var res = thing.stream().filter(a -> a.hasName(name)).findFirst();

		this.current = res.orElseThrow(() -> new RuntimeException(
			"Element " + name + " is not present in "
				+ UtlReflection.getSimpleName(this.current.getClass()) + ' ' + this.current.getName())
		);
	}
}
