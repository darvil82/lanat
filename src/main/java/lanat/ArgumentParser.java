package lanat;


import fade.mirror.MClass;
import fade.mirror.MField;
import fade.mirror.filter.Filter;
import lanat.parsing.TokenType;
import lanat.parsing.errors.ErrorHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static fade.mirror.Mirror.mirror;

public class ArgumentParser extends Command {
	private boolean isParsed = false;
	private @Nullable String license;
	private @Nullable String version;


	public ArgumentParser(@NotNull String programName, @Nullable String description) {
		super(programName, description);
	}

	public ArgumentParser(@NotNull String programName) {
		this(programName, null);
	}


	/**
	 * {@link ArgumentParser#parse(String)}
	 */
	public @NotNull ArgumentParser.AfterParseOptions parse(@NotNull String @NotNull [] args) {

		// if we receive the classic args array, just join it back
		return this.parse(String.join(" ", args));
	}

	/**
	 * Parses the given command line arguments and returns a {@link ParsedArguments} object.
	 *
	 * @param args The command line arguments to parse.
	 */
	public @NotNull ArgumentParser.AfterParseOptions parse(@NotNull String args) {
		if (this.isParsed) {
			// reset all parsing related things to the initial state
			this.resetState();
		}

		// pass the properties of this Sub-Command to its children recursively (most of the time this is what the user will want)
		this.passPropertiesToChildren();
		this.tokenize(args); // first. This will tokenize all Sub-Commands recursively
		var errorHandler = new ErrorHandler(this);
		this.parseTokens(); // same thing, this parses all the stuff recursively

		this.invokeCallbacks();

		this.isParsed = true;

		return new AfterParseOptions(errorHandler);
	}

	/**
	 * Parses the arguments from the <code>sun.java.command</code> system property.
	 */
	public @NotNull ArgumentParser.AfterParseOptions parse() {
		final var args = System.getProperty("sun.java.command").split(" ");
		return this.parse(Arrays.copyOfRange(args, 1, args.length));
	}


	@Override
	@NotNull
	ParsedArgumentsRoot getParsedArguments() {
		return new ParsedArgumentsRoot(
			this,
			this.getParser().getParsedArgumentsHashMap(),
			this.subCommands.stream().map(Command::getParsedArguments).toList(),
			this.getForwardValue()
		);
	}

	private @Nullable String getForwardValue() {
		final var tokens = this.getFullTokenList();
		final var lastToken = tokens.get(tokens.size() - 1);

		if (lastToken.type() == TokenType.FORWARD_VALUE)
			return lastToken.contents();

		return null;
	}

	public @Nullable String getLicense() {
		return this.license;
	}

	public void setLicense(@NotNull String license) {
		this.license = license;
	}

	public @Nullable String getVersion() {
		return this.version;
	}

	public void setVersion(@NotNull String version) {
		this.version = version;
	}


	public class AfterParseOptions {
		private final List<@NotNull String> errors;
		private final int errorCode;

		private AfterParseOptions(ErrorHandler errorHandler) {
			this.errorCode = ArgumentParser.this.getErrorCode();
			this.errors = errorHandler.handleErrors();
		}

		public @NotNull List<@NotNull String> getErrors() {
			return this.errors;
		}

		public boolean hasErrors() {
			return this.errorCode != 0;
		}

		public AfterParseOptions printErrors() {
			for (var error : this.errors) {
				System.err.println(error);
			}
			return this;
		}

		public AfterParseOptions exitIfErrors() {
			if (this.errorCode != 0)
				System.exit(this.errorCode);

			return this;
		}

		public @NotNull ParsedArgumentsRoot getParsedArguments() {
			return ArgumentParser.this.getParsedArguments();
		}

		public <T extends CommandTemplate> T into(@NotNull Class<T> clazz) {
			return this.into(mirror(clazz));
		}

		public <T extends CommandTemplate> T into(@NotNull MClass<T> clazz) {
			final var ctor = clazz.getConstructor();

			if (ctor.isEmpty())
				throw new IllegalArgumentException("the given class does not have a public constructor without parameters");

			final var fields = clazz.getFields(Filter.forFields().withAnnotation(Argument.Define.class)).toList();
			final var parsedArguments = this.getParsedArguments();
			final T instance = ctor.get().invoke();

			assert instance != null;

			fields.forEach(f -> {
				@SuppressWarnings("OptionalGetWithoutIsPresent") var annotation = f.getAnnotationOfType(Argument.Define.class).get();
				Arrays.stream(annotation.names()).forEach(name -> {
					var value = parsedArguments.get(name);
					if (value != null) {
						((MField<Object>)f).setValue(instance, value);
					}
				});
			});

			return instance;
		}
	}
}