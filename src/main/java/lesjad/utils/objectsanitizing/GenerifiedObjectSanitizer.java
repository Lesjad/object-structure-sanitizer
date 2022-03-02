package lesjad.utils.objectsanitizing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class GenerifiedObjectSanitizer implements ObjectStructureSanitizer {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public Object refactorObject(Object o, Predicate<Object> nestingLimit, Map<Predicate<Object>, Function<Object, Object>> modificationsMap) {
    Field[] fields;
//    List<String> countNonEmptyFields = new ArrayList<>();

    if (o==null) {
      logger.info("returning object unmodified because it is null");
      return null;
    } else {
      logger.info("started refactoring object" + o);
    }

    logger.info("deepness.test(" + o.getClass().getSimpleName() + ")");
    if (nestingLimit.test(o)) {
      logger.info("returning object unmodified because deepness evaluated to TRUE");
      return o;
    }

    try{
      fields = o.getClass().getDeclaredFields();
    } catch (NullPointerException e){
      logger.info("NullPointerException while getDeclaredFields()");
      return o;
    }

    for (Field field : fields) {
      try {
        field.setAccessible(true);

        //set an instance of the field for further use;
        Object actualField = null;
        try {
          actualField = field.get(o);
        } catch (NullPointerException e){
          //should never happen since o is checked above for not being null;
          logger.error("should never happen since o is checked above for not being null");
        }

        if (actualField==null){
          logger.info(String.format("value of actualField%s, %s is null", field.getType().getSimpleName(), field.getName()));
        } else {
          logger.info(String.format("value of actualField %s %s = %s", field.getType().getSimpleName(), field.getName(), actualField));
        }
        if (field.getType().getDeclaredFields().length == 0){
          logger.info(field.getName() + " .getType().getDeclaredFields().length == 0");
          logger.info("name = " + field.getType().getName());
        }
        //If field has subfields && is not part of Java libraries, and is not Enum - go recursively deeper
        if (actualField!=null
//                && field.getType().getDeclaredFields().length != 0
                && !nestingLimit.test(actualField)) {
          try {
            actualField = refactorObject(actualField, nestingLimit, modificationsMap);
            if (actualField!=null)
//              countNonEmptyFields.add(String.format("%s : %s", field.getName(), actualField));
            field.set(o, actualField);
          } catch (IllegalAccessException e) {
            e.printStackTrace(); // todo: sensowna obsługa błędu, zastanowić się szczególnie w kontekscie field.get(o) jakie błędy jeszcze należy obsłużyć
          }
        } else {
          Object finalActualField = actualField;
          modificationsMap.forEach((predicate, function) -> {
            logger.info(String.format("working on %s with lambda function", field.getName()));
//            logger.info(String.format("working on %s %s with lambda function", finalActualField.getClass().getSimpleName(), field.getName()));
            if (predicate.test(finalActualField)) {
              logger.info("predicate result TRUE");
              try {
                field.set(o, function.apply(finalActualField));
              } catch (IllegalAccessException | NullPointerException e) {
                logger.error("tera tutej");
                e.printStackTrace();
              }
            } else {
              logger.info("predicate result - FALSE");
            }
          });
        }
      } catch (IllegalAccessException | SecurityException e){
        logger.error(String.format("could not access the Field %s of type %s. Action aborted", field.getName(), field.getType()));
      } catch (NullPointerException e){
        logger.error(String.format("NullPointerException while working on %s %s", field.getType().getSimpleName(), field.getName()));
        e.printStackTrace();
      }
    }
    return o;
  }
}
