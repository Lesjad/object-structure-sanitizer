package lesjad.utils.refactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Sanitization_RemoveEmptyStrings implements SanitizationConditions{

    protected static Logger logger = LoggerFactory.getLogger(Sanitization_RemoveEmptyStrings.class);
    //MAPING WHERE TO USE ACTIONS
    Map<Predicate<Object>, Consumer<Object>> conditionalActions = new HashMap<>();

    public Sanitization_RemoveEmptyStrings() {
        conditionalActions.put(
                predicateWrapper(
                UsefullPredicates.IS_NULL.p.negate().or(
                        UsefullPredicates.IS_PRIMITIVE.p.negate())),
                consumerWrapper(
                ActionToPerform.REPLACE_EMPTY_STRING_WITH_NULL.f.andThen(
                        o -> {
                            Consumer<Object> objectConsumer = UsefullPredicates.IS_NULL.p.test(o) ?
                                    ActionToPerform.DO_NOTHING.f :
                                    ActionToPerform.REPLACE_WITH_NULL_IF_ALL_NONE_PRIMITIVE_FIELDS_ARE_EMPTY.f;
                            objectConsumer.accept(o);
                        })));
    }

    public Sanitization_RemoveEmptyStrings(Map<Predicate<Object>, Consumer<Object>> conditionalActions) {
        this.conditionalActions = conditionalActions;
    }



    //Interface methods
    @Override
    public Predicate<Object> nestingLimit() {
        return o -> {
            logger.info("predicate deepness: " + o);
//            logger.info(String.format("deepness() on %s %s", o.getClass().getSimpleName(), o));
            return UsefullPredicates.IS_NULL.p.or(
                    UsefullPredicates.IS_PRIMITIVE.p.or(
                            UsefullPredicates.IS_IN_JAVA_PACKAGE.p.or(
                                    UsefullPredicates.IS_ENUM.p))).test(o);
        };
    }

    @Override
    public List<Map<Predicate<Object>, Consumer<Object>>> listOfConditionalActions() {
        return List.of(conditionalActions);
    }

    @Override
    public Map<Predicate<Object>, Consumer<Object>> conditionalAction() {
        return conditionalActions;
    }

    @Override
    public Consumer<Object> singleAction() {
        return null;
    }

    private enum UsefullPredicates {
        IS_NULL(o -> {
            logger.info("predicate IS_NULL: " + o);
//            logger.info(String.format("using IS_NULL on %s %s", o.getClass().getSimpleName(), o) );
            logger.info("IS_NULL result: " + (o == null));
            return o==null;
        }),
        ALWAYS_TRUE(
                o -> {
                    logger.info("predicate ALWAYS_TRUE: " + o);
//                    logger.info(String.format("using ALWAYS_TRUE on %s %s", o.getClass().getSimpleName(), o) );
                    return true;}),
        IS_PRIMITIVE(
                o -> {
                    logger.info("predicate IS_PRIMITIVE: " + o);
//                    logger.info(String.format("using IS_PRIMITIVE on %s %s", o.getClass().getSimpleName(), o) );
                    logger.info("class of object : " + o.getClass());
                    logger.info("IS_PRIMITIVE result: "+o.getClass().isPrimitive());
                    return o.getClass().isPrimitive();}),
        IS_ENUM(
                o -> {
                    logger.info("predicate IS_ENUM: " + o);
//                    logger.info(String.format("using IS_ENUM on %s %s", o.getClass().getSimpleName(), o) );
                    logger.info("IS_ENUM result: "+o.getClass().isEnum());
                    return o.getClass().isEnum();
                }),
        IS_IN_JAVA_PACKAGE(
                o -> {
                    logger.info("predicate IS_IN_JAVA_PACKAGE: " + o);
//                    logger.info(String.format("using IS_IN_JAVA_PACKAGE on %s %s", o.getClass().getSimpleName(), o) );
                    logger.info("package name: " +o.getClass().getPackageName());
                    return o.getClass().getPackageName().startsWith("java.");
                });

        private Predicate<Object> p;
//        private String identifier;

        UsefullPredicates(Predicate<Object> p){//, String identifier) {
            this.p = p;
//            this.identifier = identifier;
        }

//        public Predicate<Object> getP() {
//            return p;
//        }

//        public String getIdentifier(){
//            return this.identifier;
//        }
    }

    private enum ActionToPerform {
        REPLACE_EMPTY_STRING_WITH_NULL(o -> {
            logger.info("action REPLACE_EMPTY_STRING_WITH_NULL: " + o);
//            logger.info(String.format("applying REPLACE_EMPTY_STRING_WITH_NULL on %s %s", o.getClass().getSimpleName(), o) );
            o = o instanceof String?
                    ((String) o).isBlank() ? null : o
                    : o;
        }),
        REPLACE_WITH_NULL_IF_ALL_NONE_PRIMITIVE_FIELDS_ARE_EMPTY(
                o -> {
                    logger.info("action REPLACE_WITH_NULL_IF_ALL_NONE_PRIMITIVE_FIELDS_ARE_EMPTY: " + o);
//                    logger.info(String.format("applying REPLACE_WITH_NULL_IF_ALL_NONE_PRIMITIVE_FIELDS_ARE_EMPTY on %s %s", o.getClass().getSimpleName(), o) );
                    o = Arrays.stream(o.getClass().getDeclaredFields()).anyMatch(
                            field -> !UsefullPredicates.IS_PRIMITIVE.p
                                    .or(UsefullPredicates.IS_ENUM.p).test(field) && !UsefullPredicates.IS_NULL.p.test(field)) ? o : null;
                }
        ),
        DO_NOTHING(o -> {});

        private Consumer<Object> f;

        ActionToPerform(Consumer<Object> f){
            this.f = f;
        }
    }

    private Consumer<Object> consumerWrapper(Consumer<Object> action){
        return o -> {
            try {
                action.accept(o);
            } catch (NullPointerException e) {
                logger.error(String.format("NullPointerException in wrapper : %s", e.getMessage()));
            }
        };
    }

    //wrapper class to work with errors
    private Predicate<Object> predicateWrapper(Predicate<Object> pred){
        return o -> {
            try {
                return pred.test(o);
            } catch (NullPointerException e) {
                logger.error(String.format("NullPointerException in predicate wrapper : %s", e.getMessage()));
                logger.error("Due to NPE cannot estimate value.. should abort program - will return false for now.");//todo: think of a solution
            }
            return false;
        };
    }
}
