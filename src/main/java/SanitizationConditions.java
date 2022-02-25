import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public interface SanitizationConditions {
    Predicate<Object> deepness();
    List<Map<Predicate<Object>, Function<Object, Object>>> listOfConditionalActions();
    Map<Predicate<Object>, Function<Object, Object>> conditionalAction();
    Function<Object, Object> singleAction();
}
