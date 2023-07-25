package lanat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This interface is used for getting the parent object of an object. This interface also provides a method with a
 * default implementation for getting the root object.
 *
 * @param <T> The type of the parent object.
 */
public interface ParentElementGetter<T extends ParentElementGetter<T>> {
	/**
	 * Gets the parent object of this object.
	 *
	 * @return The parent object of this object.
	 */
	@Nullable T getParent();

	/**
	 * Gets the root object in the hierarchy it belongs to. If this object is already the root, then this object is
	 * returned.
	 *
	 * @return The root object of this element.
	 */
	@SuppressWarnings("unchecked")
	default @NotNull T getRoot() {
		T root = (T)this;
		T parent = root.getParent();

		while (parent != null) {
			root = parent;
			parent = root.getParent();
		}

		return root;

	}
}
