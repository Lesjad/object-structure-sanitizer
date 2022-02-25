import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class Sanitization_RemoveEmptyStrings implements SanitizationConditions{

    //MAPING WHERE TO USE ACTIONS
    Map<Predicate<Object>, Function<Object, Object>> conditionalActions = new HashMap<>();

    public Sanitization_RemoveEmptyStrings() {
        conditionalActions.put(UsefullPredicates.ALWAYS_TRUE.p, ActionToPerform.REPLACE_EMPTY_STRING_WITH_NULL.f.andThen(ActionToPerform.REPLACE_WITH_NULL_IF_ALL_NONE_PRIMITIVE_FIELDS_ARE_EMPTY.f));
    }

    public Sanitization_RemoveEmptyStrings(Map<Predicate<Object>, Function<Object, Object>> conditionalActions) {
        this.conditionalActions = conditionalActions;
    }



    //Interface methods
    @Override
    public Predicate<Object> deepness() {
        return o -> UsefullPredicates.IS_PRIMITIVE.p.or(
                UsefullPredicates.IS_IN_JAVA_PACKAGE.p.or(
                        UsefullPredicates.IS_ENUM.p)).test(o);
    }

    @Override
    public List<Map<Predicate<Object>, Function<Object, Object>>> listOfConditionalActions() {
        return List.of(conditionalActions);
    }

    @Override
    public Map<Predicate<Object>, Function<Object, Object>> conditionalAction() {
        return conditionalActions;
    }

    @Override
    public Function<Object, Object> singleAction() {
        return null;
    }

    private enum UsefullPredicates {
        ALWAYS_TRUE(o -> true),
        IS_PRIMITIVE(o -> o.getClass().isPrimitive()),
        IS_ENUM(o -> o.getClass().isEnum()),
        IS_IN_JAVA_PACKAGE(o -> o.getClass().getPackageName().startsWith("java."));

        private Predicate<Object> p;

        private UsefullPredicates(Predicate<Object> p) {
            this.p = p;
        }

        public Predicate<Object> getP() {
            return p;
        }
    }

    private enum ActionToPerform {
        REPLACE_EMPTY_STRING_WITH_NULL(o -> {
            o = o.toString().isBlank() ? null : o;
            return o;
        }),
        REPLACE_WITH_NULL_IF_ALL_NONE_PRIMITIVE_FIELDS_ARE_EMPTY(
                o -> {
                    o = Arrays.stream(o.getClass().getDeclaredFields()).anyMatch(UsefullPredicates.IS_PRIMITIVE.p.negate().or(UsefullPredicates.IS_ENUM.p.negate())) ? o : null;
                    return o;
                }
        ),
        DO_NOTHING(o -> o);

        private Function<Object, Object> f;

        ActionToPerform(Function<Object, Object> f){
            this.f = f;
        }
    }
}
