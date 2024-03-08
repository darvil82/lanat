package lanat.helpRepresentation.descriptions;

import lanat.Argument;
import lanat.ArgumentType;
import lanat.Command;
import lanat.helpRepresentation.descriptions.exceptions.InvalidRouteException;
import lanat.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.UtlReflection;
import utils.UtlString;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Parser for simple route syntax used in description tags (e.g. {@code args.myArg1.type}).
 * <p>
 * The route syntax is very simple. It is a dot-separated list of names indicating the path to the object to be
 * returned. By default, the route initial target is the command the user belongs to. If the route starts with
 * {@code !}, the user itself becomes the initial target. If the route is empty or null, the command the user
 * belongs to is returned.
 * </p>
 * <p>
 * These are the objects that can be accessed using the route syntax:
 * <ul>
 * <li>{@code args}: the arguments of the command.
 * <ul>
 * <li>{@code type}: the type of the argument.
 * <p>
 * Note that this only works if the current target is an {@link Argument}.
 * </p>
 * </li>
 * </ul>
 * </li>
 * <li>{@code groups}: the groups of the command.</li>
 * <li>
 * {@code cmds}: the subcommands of the command.
 * <p>
 * Note that after selecting a command, all the selectors above can be used again to select inner objects of the command.
 * </p>
 * </li>
 * </ul>
 *
 * <h2>Examples</h2>
 * <ol>
 * <li>
 * Select the type of the argument {@code myArg1} of the current command:
 * {@code "args.myArg1.type"}
 * </li>
 * <li>
 * Select the Sub-Command {@code myCmd} of the current command:
 * {@code "cmds.myCmd"}
 * </li>
 * <li>
 * Select the type of the argument {@code myArg1} of the Sub-Command {@code myCmd} of the current command:
 * {@code "cmds.myCmd.args.myArg1.type"}
 * </li>
 * <li>
 * Select the {@link ArgumentType} of the {@link Argument} that is requesting to parse this description:
 * {@code "!.type"}
 * </li>
 * </ol>
 */
public class RouteParser {
	/** The current object being handled in the route */
	private @NotNull NamedWithDescription currentTarget;
	private final @NotNull String[] route;
	private int index;

	private static final char SEPARATOR = '.';
	private static final String SELF_SELECTOR = "!";


	private RouteParser(@NotNull NamedWithDescription user, @Nullable String route) {
		// if route is empty, the command the user belongs to is the target
		if (UtlString.isNullOrBlank(route)) {
			this.currentTarget = RouteParser.getCommandOf(user);
			this.route = new String[0];
			return;
		}

		final String[] splitRoute = UtlString.split(route, SEPARATOR);

		// if route starts with the user selector, the user itself is the target
		if (splitRoute[0].equals(SELF_SELECTOR)) {
			this.currentTarget = user;
			// slice the array to remove the first element (the user selector string)
			this.route = Arrays.copyOfRange(splitRoute, 1, splitRoute.length);
			return;
		}

		this.currentTarget = RouteParser.getCommandOf(user);
		this.route = splitRoute;
	}

	/**
	 * Parses a route and returns the object it points to. If the route is empty or null, the command the user belongs
	 * to is returned.
	 * <p>
	 * The reason why the user is needed is because its likely that it will be needed to gather the Command it belongs
	 * to, and also if the route starts with {@code !}, the user itself becomes the initial target.
	 * </p>
	 *
	 * @param user the user that is requesting to parse the route
	 * @param route the route to parse
	 * @return the object the route points to
	 * @see RouteParser
	 */
	public static NamedWithDescription parse(@NotNull NamedWithDescription user, @Nullable String route) {
		return new RouteParser(user, route).parse();
	}

	/**
	 * Returns the command the object belongs to. If the object is a {@link Command}, it is returned. If it is a
	 * {@link CommandUser}, the command it belongs to is returned. Otherwise, an {@link InvalidRouteException} is
	 * thrown.
	 *
	 * @param obj the object to get the command of
	 * @return the command the object belongs to
	 * @throws InvalidRouteException if the object is not a {@link Command} or a {@link CommandUser}
	 */
	public static Command getCommandOf(NamedWithDescription obj) {
		if (obj instanceof Command cmd) {
			return cmd;
		} else if (obj instanceof CommandUser cmdUser) {
			return cmdUser.getParentCommand();
		}

		throw new InvalidRouteException("Cannot get the Command " + obj.getName() + " belongs to");
	}

	/**
	 * Advances through the route and sets the current target to each element in the route. If the route is invalid, an
	 * {@link InvalidRouteException} is thrown.
	 *
	 * @return the object the route points to
	 */
	private NamedWithDescription parse() {
		for (this.index = 0; this.index < this.route.length; this.index++) {
			final String token = this.route[this.index];

			if (token.equals("args") && this.currentTarget instanceof ArgumentAdder argsContainer)
				this.setCurrentTarget(argsContainer.getArguments(), MultipleNamesAndDescription::hasName);
			else if (token.equals("groups") && this.currentTarget instanceof ArgumentGroupAdder groupsContainer)
				this.setCurrentTarget(groupsContainer.getGroups(), (g, name) -> g.getName().equals(name));
			else if (token.equals("cmds") && this.currentTarget instanceof Command cmdsContainer)
				this.setCurrentTarget(cmdsContainer.getCommands(), MultipleNamesAndDescription::hasName);
			else if (token.equals("type") && this.currentTarget instanceof Argument<?, ?> arg)
				this.currentTarget = arg.type;
			else
				throw new InvalidRouteException(this.currentTarget, token);
		}

		return this.currentTarget;
	}

	/**
	 * Sets the current target to the first element in the list that matches the given predicate.
	 *
	 * @param list the list to search in
	 * @param predicate the predicate to use to match the elements. The first parameter is the element, the second is
	 * 	the name to match against.
	 * @param <E> the type of the elements in the list
	 */
	private <E extends NamedWithDescription>
	void setCurrentTarget(List<E> list, BiFunction<E, String, Boolean> predicate) {
		if (this.index + 1 >= this.route.length)
			throw new InvalidRouteException(this.currentTarget, "", "Expected a name");

		final var name = this.route[++this.index];
		final Optional<E> res = list.stream().filter(x -> predicate.apply(x, name)).findFirst();

		this.currentTarget = res.orElseThrow(() -> new RuntimeException(
			"Element '" + name + "' is not present in "
				+ UtlReflection.getSimpleName(this.currentTarget.getClass()) + " '" + this.currentTarget.getName() + "'"
		));
	}
}