package lesjad.utils.refactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class GenericSanitizer implements ObjectStructureSanitizer {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Object refactorObject(Object o, Predicate<Object> nestingLimit, Map<Predicate<Object>, Consumer<Object>> modificationsMap) {
        Field[] fields;

        //todo: describe in the documentation that usefull to define in nesting limit is to omit null objects unless you'd like to do something with them
//        if (o == null) {
//            logger.info("returning object unmodified because it is null");
//            return null;
//        }
        logger.debug(String.format("started refactoring object %s", o));

        logger.debug("attempt nestingLimit.test("+o+")");
        if (nestingLimit.test(o)) {
            logger.debug("nesting limit reached - applying modificationsMap");
            modificationsMap.forEach((predicate, consumer) -> {
                if (predicate.test(o))
                    consumer.accept(o);
            });
            return o;
        }
        try {
            fields = o.getClass().getDeclaredFields();
        } catch (NullPointerException e) {
            logger.debug(String.format("NullPointerException triggered while reading declared fields of object %s", o), e);
            fields = new Field[0];
        } catch (SecurityException e) {
            logger.debug(String.format("Security settings blocked to read declared fields of object %s", o), e);
            fields = new Field[0];
        }

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
