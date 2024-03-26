package lanat;

import lanat.parsing.Tokenizer;
import lanat.parsing.errors.ErrorCollector;
import lanat.utils.UtlMisc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;


/**
 * <h2>Argument Parser</h2>
 * <p>
 * Provides the ability to parse a command line input and later gather the values of the parsed arguments.
 * @see Command
 */
public class ArgumentParser extends Command {
	/** This is used to be able to tell if we should reset the state of all the commands before parsing */
	private boolean isParsed = false;
	private @Nullable String details;
	private @Nullable String version;

	/**
	 * Creates a new command with the given name and description.
	 * @param programName The name of the command. This is the name the user will use to indicate that they want to use this
	 * 		   command.
	 * @param description The description of the command.
	 * @see #setDescription(String)
	 */
	public ArgumentParser(@NotNull String programName, @Nullable String description) {
		super(programName, description);
	}

	/**
	 * Creates a new command with the given name and no description. This is the name the user will use to
	 * indicate that they want to use this command.
	 * @param programName The name of the command.
	 */
	public ArgumentParser(@NotNull String programName) {
		this(programName, null);
	}

	/**
	 * Creates a new command based on the given {@link CommandTemplate}. This does not take Sub-Commands into account.
	 * If you want to add Sub-Commands, use {@link ArgumentParser#from(Class)} instead.
	 * @param templateClass The class of the template to use.
	 * @see CommandTemplate
	 */
	public ArgumentParser(@NotNull Class<? extends CommandTemplate> templateClass) {
		super(templateClass);
	}

	/**
	 * Constructs a new {@link ArgumentParser} based on the given {@link CommandTemplate}, taking Sub-Commands into
	 * account.
	 * <p>
	 * This is basically a shortcut for the following code:
	 * <pre>{@code
	 * new ArgumentParser(clazz) {{
	 *     this.addCommand(new Command(subCmdClazz)); // do this for all possible sub-commands
	 * }};
	 * }</pre>
	 * This method basically makes it easier to add Sub-Commands to the given {@link CommandTemplate}. It looks for
	 * {@link lanat.CommandTemplate.CommandAccessor} annotations in the given class and adds the corresponding
	 * sub-commands to the {@link Command} object. This is done recursively.
	 * @param templateClass The class of the {@link CommandTemplate} to use.
	 * @return A new {@link ArgumentParser} based on the given {@link CommandTemplate}.
	 * @see CommandTemplate
	 */
	public static ArgumentParser from(@NotNull Class<? extends CommandTemplate> templateClass) {
		final var argParser = new ArgumentParser(templateClass);

		// add all commands recursively
		ArgumentParser.from$setCommands(templateClass, argParser);

		return argParser;
	}

	/**
	 * Constructs a new {@link ArgumentParser} based on the given {@link CommandTemplate}, parses the given input, and
	 * populates the template with the parsed values.
	 * <p>
	 * This is basically a shortcut for the following code:
	 * <pre>{@code
	 * ArgumentParser.from(clazz).parse(input).into(clazz);
	 * }</pre>
	 * <h4>Example:</h4>
	 * This code:
	 * <pre>{@code
	 * ArgumentParser.from(MyTemplate.class)
	 *     .parse(input)
	 *     .withActions(AfterParseOptions.DEFAULT_ACTIONS)
	 *     .into(MyTemplate.class);
	 * }</pre>
	 * <p>
	 * Is equivalent to this code:
	 * <pre>{@code
	 * MyTemplate parsed = ArgumentParser.parseFromInto(MyTemplate.class, input);
	 * }
	 * </pre>
	 * The example above uses the {@link #parseFromInto(Class, CLInput)} overload, which sets the default actions for
	 * the {@link AfterParseOptions} object.
	 * <p>
	 * This method uses {@link #from(Class)}. See that method for more info.
	 * @param templateClass The class to use as a template.
	 * @param input The input to parse.
	 * @param actions A consumer that can be used for configuring the parsing process.
	 * @param <T> The type of the template.
	 * @return The parsed template.
	 * @see #parseFromInto(Class, CLInput)
	 * @see CommandTemplate
	 * @see #from(Class)
	 * @see AfterParseOptions
	 */
	public static <T extends CommandTemplate> @NotNull T parseFromInto(
		@NotNull Class<T> templateClass,
		@NotNull CLInput input,
		@NotNull Consumer<AfterParseOptions.@NotNull AfterParseActions> actions
	) {
		return ArgumentParser.from(templateClass)
			.parse(input)
			.withActions(actions)
			.into(templateClass);
	}

	/**
	 * Constructs a new {@link ArgumentParser} based on the given {@link CommandTemplate}, parses the given input, and
	 * populates the template with the parsed values. Uses the default options for the {@link AfterParseOptions} object
	 * ({@link AfterParseOptions#DEFAULT_ACTIONS}).
	 * <p>
	 * See {@link #parseFromInto(Class, CLInput, Consumer)} for more info.
	 * @param templateClass The class to use as a template.
	 * @param input The input to parse.
	 * @param <T> The type of the template.
	 * @return The parsed template.
	 * @see CommandTemplate
	 * @see #parseFromInto(Class, CLInput, Consumer)
	 */
	public static <T extends CommandTemplate>
	@NotNull T parseFromInto(@NotNull Class<T> templateClass, @NotNull CLInput input) {
		return ArgumentParser.parseFromInto(templateClass, input, AfterParseOptions.DEFAULT_ACTIONS);
	}

	/**
	 * Constructs a new {@link ArgumentParser} based on the given {@link CommandTemplate}, parses the given input, and
	 * populates the template with the parsed values.
	 * <p>
	 * See {@link #parseFromInto(Class, CLInput, Consumer)} for more info.
	 * @param templateClass The class to use as a template.
	 * @param input The input to parse.
	 * @param <T> The type of the template.
	 * @return The parsed template.
	 * @see CommandTemplate
	 * @see #parseFromInto(Class, CLInput, Consumer)
	 */
	public static <T extends CommandTemplate>
	@NotNull T parseFromInto(@NotNull Class<T> templateClass, @NotNull String @NotNull [] input) {
		return ArgumentParser.parseFromInto(templateClass, CLInput.from(input));
	}

	/**
	 * Adds all commands defined with {@link Command.Define} in the given class to the given parent command. This method
	 * is recursive and will add all sub-commands of the given class.
	 *
	 * @param templateClass The class to search for commands in.
	 * @param parentCommand The command to add the found commands to.
	 * @param <T> The type of the class to search for commands in.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends CommandTemplate>
	void from$setCommands(@NotNull Class<T> templateClass, @NotNull Command parentCommand) {
		Stream.of(templateClass.getDeclaredClasses())
			.filter(c -> c.isAnnotationPresent(Command.Define.class))
			.filter(c -> Modifier.isStatic(c.getModifiers()))
			.filter(CommandTemplate.class::isAssignableFrom)
			.map(c -> (Class<? extends CommandTemplate>)c)
			.forEach(cmdDef -> {
				var command = new Command(cmdDef);
				parentCommand.addCommand(command);
				ArgumentParser.from$setCommands(cmdDef, command);
			});
	}


	/**
	 * Parses the given command line arguments and returns a {@link AfterParseOptions} object.
	 *
	 * @param input The command line arguments to parse.
	 * @see AfterParseOptions
	 */
	public @NotNull AfterParseOptions parse(@NotNull CLInput input) {
		if (this.isParsed) {
			// reset all parsing related things to the initial state
			this.resetState();
		}

		// pass the properties of this Sub-Command to its children recursively (most of the time this is what the user will want)
		this.passPropertiesToChildren();
		this.linkGroupHierarchy();

		this.tokenize(input.args); // first. This will tokenize all Sub-Commands recursively

		var errorCollector = new ErrorCollector(this.getFullTokenList(), input.args);

		// do not parse anything if there are any errors in the tokenizer
		if (this.tokenizationSucceeded()) {
			this.parseTokens(); // same thing, this parses all the stuff recursively

			// cache the parsed args map. Generating this calls Argument#finishParsing(), which is important
			// to call before doing other stuff
			this.generateParsedArgsMap();
		}

		this.getTokenizer().getTokenizedCommands().forEach(errorCollector::collect);
		this.isParsed = true;

		return new AfterParseOptions(this, errorCollector, !input.isEmpty());
	}

	private boolean tokenizationSucceeded() {
		return this.getTokenizer().getTokenizedCommands().stream()
			.map(Command::getTokenizer)
			.noneMatch(Tokenizer::hasDisplayErrors);
	}

	private void tokenize(@NotNull String args) {
		this.getTokenizer().tokenize(args, null);
	}

	private void parseTokens() {
		// first, we need to set the tokens of all tokenized subCommands
		Command cmd = this;
		do {
			cmd.getParser().setTokens(cmd.getTokenizer().getFinalTokens());
		} while ((cmd = cmd.getTokenizer().getTokenizedSubCommand()) != null);

		// this parses recursively!
		this.getParser().parseTokens(null);
	}

	@Override
	@NotNull ParseResultRoot getParseResult() {
		return new ParseResultRoot(
			this,
			this.getParser().getParsedArgsMap(),
			this.getCommands().stream().map(Command::getParseResult).toList(),
			this.getForwardValue()
		);
	}

	/**
	 * Returns the forward value of the last parsed argument.
	 */
	private @Nullable String getForwardValue() {
		// the forward value is only present on the last parsed command
		return UtlMisc.last(this.getTokenizer().getTokenizedCommands()).getParser().getForwardValue();
	}

	/**
	 * Sets the details of this program. By default, this is shown in the help message.
	 * @param details The details text content to set.
	 */
	public void setDetails(@NotNull String details) {
		this.details = details;
	}

	/**
	 * Returns the details of this program.
	 * @see #setDetails(String)
	 */
	public @Nullable String getDetails() {
		return this.details;
	}

	/**
	 * Sets the version of this program. By default, this is shown in the help message.
	 * @param version The version information to set.
	 */
	public void setVersion(@NotNull String version) {
		this.version = version;
	}

	/**
	 * Adds a 'version' argument which shows the version of the program
	 * (provided by the {@link ArgumentParser#getVersion()} method), and then exits the program with the given return
	 * code.
	 * @param returnCode The return code to exit the program with.
	 */
	public void addVersionArgument(int returnCode) {
		this.addArgument(Argument.createOfActionType("version")
			.onOk(t -> {
				System.out.println("Version: " + Objects.requireNonNullElse(this.getVersion(), "unknown"));
				System.exit(returnCode);
			})
			.description("Shows the version of this program.")
			.unique(true)
		);
	}

	/**
	 * Returns the version of this program.
	 * @see #setVersion(String)
	 */
	public @Nullable String getVersion() {
		return this.version;
	}

	@Override
	public boolean isRoot() {
		return true; // this is always the root command
	}
}