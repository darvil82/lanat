package lanat;

import lanat.exceptions.ArgumentNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

public abstract class CommandTemplate {
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface InitDef {
		boolean callSuper() default true;
	}

	public record CommandBuildHelper(@NotNull Command cmd, @NotNull List<Argument.ArgumentBuilder<?, ?>> args) {
		public Argument.ArgumentBuilder<?, ?> getArgument(@NotNull String name) {
			return this.args.stream()
				.filter(a -> a.hasName(name))
				.findFirst()
				.orElseThrow(() -> new ArgumentNotFoundException(name));
		}
	}

	@InitDef
	public static void init(@NotNull CommandBuildHelper helper) {}
}