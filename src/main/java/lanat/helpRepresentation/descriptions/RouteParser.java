package lanat.helpRepresentation.descriptions;

import lanat.*;
import lanat.helpRepresentation.descriptions.exceptions.InvalidRouteException;
import lanat.utils.UtlReflection;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

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
	NamedWithDescription parse(T runner, String route) {
		return new RouteParser<>(runner, route).parse();
	}


	private NamedWithDescription parse() {
		for (this.index = 0; this.index < this.route.length; this.index++) {
			final String token = this.route[this.index];

			if (token.equals("args") && this.current instanceof ArgumentAdder argsContainer) {
				this.setCurrent(argsContainer.getArguments(), MultipleNamesAndDescription::hasName);
			} else if (token.equals("groups") && this.current instanceof ArgumentGroupAdder groupsContainer) {
				this.setCurrent(groupsContainer.getSubGroups(), (g, name) -> g.getName().equals(name));
			} else if (token.equals("cmds") && this.current instanceof Command cmdsContainer) {
				this.setCurrent(cmdsContainer.getSubCommands(), MultipleNamesAndDescription::hasName);
			} else if (token.equals("type") && this.current instanceof Argument<?, ?> arg) {
				this.current = arg.argType;
			} else {
				throw new InvalidRouteException(this.current, token);
			}
		}

		return this.current;
	}

	private <E extends NamedWithDescription>
	void setCurrent(List<E> list, BiFunction<E, String, Boolean> filter) {
		final var name = this.route[++this.index];
		final Optional<E> res = list.stream().filter(x -> filter.apply(x, name)).findFirst();

		this.current = res.orElseThrow(() -> new RuntimeException(
			"Element " + name + " is not present in "
				+ UtlReflection.getSimpleName(this.current.getClass()) + ' ' + this.current.getName())
		);
	}
}
