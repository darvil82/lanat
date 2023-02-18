package lanat.utils;

import java.util.ArrayList;
import java.util.function.Predicate;

/** A class that allows you to compare two objects using a list of predicates. */
public class Comparator<T> {
	private final T first;
	private final T second;
	private final ArrayList<Pred<T>> predicates = new ArrayList<>();

	private record Pred<T>(int priority, Predicate<T> predicateCb) {
		Pred {
			if (priority < 0) throw new IllegalArgumentException("Priority must be >= 0");
		}
	}

	private Comparator(T first, T second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Creates a new Comparator instance that will compare the two objects given.
	 * @param first The first object to compare.
	 * @param second The second object to compare.
	 * @return A new Comparator instance.
	 * @param <T> The type of the objects to compare.
	 */
	public static <T> Comparator<T> of(T first, T second) {
		return new Comparator<>(first, second);
	}

	/**
	 * Adds a predicate to the list of predicates to be used when comparing.
	 * @param p The predicate to add.
	 * @param priority The priority of the predicate. The higher the priority, the earlier the predicate will be checked.
	 */
	public Comparator<T> addPredicate(Predicate<T> p, int priority) {
		this.predicates.add(new Pred<>(priority, p));
		return this;
	}

	/**
	 * Adds a predicate to the list of predicates to be used when comparing. The priority of the predicate will be 0.
	 * @param p The predicate to add.
	 */
	public Comparator<T> addPredicate(Predicate<T> p) {
		return this.addPredicate(p, 0);
	}

	/**
	 * Compares the two objects given when creating the Comparator instance.
	 * @return -1 if the first object is "greater" than the second, 1 if the second object is "greater" than the first,
	 * 0 if they are equal.
	 */
	public int compare() {
		this.predicates.sort((a, b) -> b.priority - a.priority);
		for (final Pred<T> p : this.predicates) {
			if (p.predicateCb.test(this.first)) return -1;
			if (p.predicateCb.test(this.second)) return 1;
		}
		return 0;
	}
}
