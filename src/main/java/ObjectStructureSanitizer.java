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
 * {@link #refactorObject(Object, Predicate, List)}.
 */
@FunctionalInterface
public interface ObjectStructureSanitizer {
    /**
     * Implementation should go deep until deepness is met
     * @param o the object to be refactored
     * @param deepness condition when to stop digg deeper (for instance if class is part of certain library e.g. java.*)
     * @param function pair of {@link Predicate} and {@link Function}.
     *                 if <B>Object o</B> meets <B>Predicate</B> perform on it operation described in <B>Function</B>
     * @return refactored Object o;
     */
    Object refactorObject(Object o, Predicate<Object> deepness, List<Map<Predicate<Object>, Function<Object, Object>>> function);
}
//TODO: wyjebac liste, przeciez mapa jest juz kolekcja!
