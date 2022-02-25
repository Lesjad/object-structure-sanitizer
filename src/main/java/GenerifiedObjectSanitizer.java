import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class GenerifiedObjectSanitizer implements ObjectStructureSanitizer {

  protected final Logger logger = Logger.getLogger(getClass().getName());

  @Override
  public Object refactorObject(Object o, Predicate<Object> deepness, List<Map<Predicate<Object>, Function<Object, Object>>> function) {
    Field[] fields;
    List<String> countNonEmptyFields = new ArrayList<>();

    if (deepness.test(o))
      return o;

    try{
      fields = o.getClass().getDeclaredFields();
    } catch (NullPointerException e){
      return o;
    }

//    if (ignoreDeleting.contains(o.getClass()))
//      countNonEmptyFields.add(String.format("Zasejwowano %s : %s", o.getClass(), o.toString()));

    for (Field field : fields) {
      try {
        field.setAccessible(true);

        //set an instance of the field for further use;
        Object actualField = null;
        try {
          actualField = field.get(o);
        } catch (NullPointerException e){
          //should never happen since o is checked above for not being null;
          logger.info("should never happen since o is checked above for not being null");
        }
        //If field has subfields && is not part of Java libraries, and is not Enum - go recursively deeper
        if (field.getType().getDeclaredFields().length != 0
                && !deepness.test(actualField)) {
          try {
            actualField = refactorObject(actualField, deepness, function);
            if (actualField!=null)
              countNonEmptyFields.add(String.format("%s : %s", field.getName(), actualField));
            field.set(o, actualField);
          } catch (IllegalAccessException e) {
            e.printStackTrace(); // todo: sensowna obsługa błędu, zastanowić się szczególnie w kontekscie field.get(o) jakie błędy jeszcze należy obsłużyć
          }
        } else {

          Object finalActualField = actualField;
          function.forEach(
                  predicateFunctionMap -> predicateFunctionMap.forEach(
                          (objectPredicate, function1) -> {
                            logger.info("working on predicate with function");
                            if (objectPredicate.test(finalActualField)) {
                              try {
                                field.set(o, function1.apply(finalActualField));
                              } catch (IllegalAccessException e) {
                                e.printStackTrace();
                              }
                            }
                          }));
          //todo:verify if all the necessary actions are complete
//          try {
//            if (field.getType().isPrimitive())
//              logger.info(String.format("found primitive type %s %s, left value as is: %s", field.getType().getName(), field.getName(),actualField));
//            else if (!List.of("null", "").contains(field.get(o).toString())) {
//              logger.info(
//                      String.format(
//                              "found value other then empty/null: %s = %s",
//                              field.getName(), field.get(o).toString()));
//              countNonEmptyFields.add(String.format("%s : %s", field.getName(), field.get(o).toString()));
//            } else {
//              logger.info(String.format("setting value of %s to null", field.getName()));
//              field.set(o, null);
//            }
//          } catch (NullPointerException e){
//            logger.info("empty catch...");
//          }
        }
      } catch (IllegalAccessException | SecurityException e){
        logger.info(String.format("could not access the Field %s of type %s. Action aborted", field.getName(), field.getType()));
      }

    }
    //todo: sprawdzic te waruneczki
    return o;
//    if (ignoreDeleting.contains(o.getClass())){
//      logger.info(String.format("sejwowany obiekt %s", o.getClass()));
//      return o;
//    } else if (countNonEmptyFields.size()>0){
//      logger.info(String.format("klasa %s ma %d niepustych pól", o.getClass(), countNonEmptyFields.size()));
//      return o;
//    } else {
//      logger.info(String.format("Klasy %s nie ma na liście, więc jest nullowana.", o.getClass()));
//      return null;
//    }
  }
}
