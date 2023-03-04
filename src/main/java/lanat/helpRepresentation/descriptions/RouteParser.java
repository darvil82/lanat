package lanat.helpRepresentation.descriptions;

import lanat.*;
import lanat.helpRepresentation.descriptions.exceptions.InvalidRouteException;
import lanat.utils.UtlReflection;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class RouteParser {
	private NamedWithDescription current;
	private final String[] route;
	private int index;

	private RouteParser(@NotNull NamedWithDescription user, @Nullable String route) {
		// if route is empty, the command the user belongs to is the target
		if (UtlString.isNullOrEmpty(route)) {
			this.current = RouteParser.getCommandOf(user);
			this.route = new String[0];
			return;
		}

		final String[] splitRoute = route.split("\\.");

		if (splitRoute[0].equals("!")) {
			this.current = user;
			this.route = Arrays.copyOfRange(splitRoute, 1, splitRoute.length);
			return;
		}

		this.current = RouteParser.getCommandOf(user);
		this.route = splitRoute;
	}

	public static NamedWithDescription parse(@NotNull NamedWithDescription user, @Nullable String route) {
		return new RouteParser(user, route).parse();
	}

	public static Command getCommandOf(NamedWithDescription obj) {
		if (obj instanceof Command cmd) {
			return cmd;
		} else if (obj instanceof CommandUser cmdUser) {
			return cmdUser.getParentCommand();
		} else {
			throw new InvalidRouteException("Cannot get the Command " + obj.getName() + " belongs to");
		}
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
	void setCurrent(List<E> list, BiFunction<E, String, Boolean> predicate) {
		if (this.index + 1 >= this.route.length)
			throw new InvalidRouteException(this.current, "", "Expected a name");

		final var name = this.route[++this.index];
		final Optional<E> res = list.stream().filter(x -> predicate.apply(x, name)).findFirst();

		this.current = res.orElseThrow(() -> new RuntimeException(
			"Element " + name + " is not present in "
				+ UtlReflection.getSimpleName(this.current.getClass()) + ' ' + this.current.getName())
		);
	}
}
