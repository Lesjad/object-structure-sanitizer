package lesjad.utils.refactor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface SanitizationConditions {
    Predicate<Object> nestingLimit();
    List<Map<Predicate<Object>, Consumer<Object>>> listOfConditionalActions();
    Map<Predicate<Object>, Consumer<Object>> conditionalAction();
    Consumer<Object> singleAction();

}
