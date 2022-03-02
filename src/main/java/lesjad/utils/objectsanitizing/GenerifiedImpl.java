package lesjad.utils.objectsanitizing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class GenerifiedImpl implements ObjectStructureSanitizer {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Object refactorObject(Object o, Predicate<Object> nestingLimit, Map<Predicate<Object>, Function<Object, Object>> modificationsMap) {
        Field[] fields;
//    List<String> countNonEmptyFields = new ArrayList<>();

        if (o == null) {
            logger.info("returning object unmodified because it is null");
            return null;
        }
        logger.info("started refactoring object" + o);

        if (nestingLimit.test(o)) {
            logger.info("nestingLimit.test("+o+")");
            modificationsMap.forEach((predicate, function) -> {
                if (predicate.test(o))
                    function.apply(o);
            });
            return o;
        }
        fields = o.getClass().getDeclaredFields();

        Arrays.stream(fields).forEach(field -> {
            logger.info(String.format("processing field: %s %s", field.getType(), field.getName()));
            field.setAccessible(true);
            try {
                refactorObject(field.get(o), nestingLimit, modificationsMap);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        return o;
    }
}
