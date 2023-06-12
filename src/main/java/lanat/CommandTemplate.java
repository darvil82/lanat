package lanat;

import lanat.exceptions.ArgumentNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Command.Define
public abstract class CommandTemplate {
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	protected @interface InitDef {}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	protected @interface CommandAccessor {}

	protected record CommandBuildHelper(@NotNull Command cmd, @NotNull List<ArgumentBuilder<?, ?>> args) {
		@SuppressWarnings("unchecked")
		public <T extends ArgumentType<TInner>, TInner>
		ArgumentBuilder<T, TInner> getArgument(@NotNull String name) {
			return (ArgumentBuilder<T, TInner>)this.args.stream()
				.filter(a -> a.hasName(name))
				.findFirst()
				.orElseThrow(() -> new ArgumentNotFoundException(name));
		}
	}

	// Dummy methods so that we prevent the user from creating an instance method with the same name.
	@InitDef
	public static void beforeInit(@NotNull CommandBuildHelper cmd) {}

	@InitDef
	public static void afterInit(@NotNull Command cmd) {}

	public static @NotNull String @NotNull [] getTemplateNames(@NotNull Class<? extends CommandTemplate> cmdTemplate) {
		final var annotationNames = cmdTemplate.getAnnotation(Command.Define.class).names();

		// if no names are specified, use the simple name of the class
		return annotationNames.length == 0 ?
			new String[] { cmdTemplate.getSimpleName() }
			: annotationNames;
	}
}