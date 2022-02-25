import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Modifies given Object according List of conditions.
 * Implementation should go deep to the lowest child object fields and perform check
 * weather the field matches the {@link Predicate}, if so it applies {@link Function}
 *
 * <p>This is a {@link FunctionalInterface} * whose functional method is
 * {@link #refactorObject(Object, List)}.
 */
@FunctionalInterface
public interface ObjectStructureSanitizer {
    /**\
     * implementation
     * @param o
     * @param function
     * @return
     */
    Object refactorObject(Object o, List<Class> ignoreDeleting) /*List<Map<Predicate<Object>, List<Function<Object, Object>>>> function)*/throws IllegalAccessException;
}
