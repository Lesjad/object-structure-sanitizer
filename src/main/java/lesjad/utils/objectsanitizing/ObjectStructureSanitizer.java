package lesjad.utils.objectsanitizing;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Modifies given Object according List of conditions.
 * Implementation should go deep to the lowest child object fields and perform check
 * weather the field matches the {@link Predicate}, if so it applies {@link Function}
 *
 * <p>This is a {@link FunctionalInterface} * whose functional method is
 * {@link #refactorObject(Object, Predicate, Map)}.
 */
@FunctionalInterface
public interface ObjectStructureSanitizer {
    /**
     * Implementation should go deep until nestingLimit is met
     * @param o the object to be refactored
     * @param nestingLimit condition when to stop digg deeper (for instance if class is part of certain library e.g. java.*)
     * @param modificationsMap pair of {@link Predicate} and {@link Function}.
     *                 if <B>Object o</B> meets <B>Predicate</B> perform operation on it described in <B>Function</B>
     * @return refactored Object o;
     */
    Object refactorObject(Object o, Predicate<Object> nestingLimit, Map<Predicate<Object>, Function<Object, Object>> modificationsMap);
}