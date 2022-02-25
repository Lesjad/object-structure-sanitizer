import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DtoCleaningUtil_NullsAndEmptyStrings implements ObjectStructureSanitizer {

  protected final Logger logger = Logger.getLogger(getClass().getName());

  @Override
  public Object refactorObject(Object o, List<Class> ignoreDeleting)/*, List<Function<Object, Object>> function)*/ throws IllegalAccessException {
    Field[] fields;
    List<String> countNonEmptyFields = new ArrayList<>();

    try{
      fields = o.getClass().getDeclaredFields();
    } catch (NullPointerException e){
      return o;
    }

    if (ignoreDeleting.contains(o.getClass()))
      countNonEmptyFields.add(String.format("Zasejwowano %s : %s", o.getClass(), o.toString()));

    for (Field field : fields) {
      field.setAccessible(true);

      //If field has subfields && is not part of Java libraries, and is not Enum - go recursively deeper
      if (field.getType().getDeclaredFields().length != 0
          && !field.getType().getPackageName().startsWith("java.")
              && !field.getType().isEnum()
      ) {
        try {
          Object temp = refactorObject(field.get(o), ignoreDeleting);
          if (temp!=null)
            countNonEmptyFields.add(String.format("%s : %s", field.getName(), field.get(o).toString()));
          field.set(o, temp);
        } catch (IllegalAccessException e) {
          e.printStackTrace(); // todo: sensowna obsługa błędu
        }
      } else if (ignoreDeleting.contains(field.getType())) {
        continue;
      } else {
        try {
          if (field.getType().isPrimitive())
            logger.info(String.format("found primitive type %s %s, left value as is: %s", field.getType().getName(), field.getName(),field.get(o).toString()));
          else if (!List.of("null", "").contains(field.get(o).toString())) {
            logger.info(
                String.format(
                    "found value other then empty/null: %s = %s",
                    field.getName(), field.get(o).toString()));
            countNonEmptyFields.add(String.format("%s : %s", field.getName(), field.get(o).toString()));
          } else {
            logger.info(String.format("setting value of %s to null", field.getName()));
            field.set(o, null);
          }
        } catch (NullPointerException e){
          logger.info("empty catch...");
        }
      }
    }
    if (ignoreDeleting.contains(o.getClass())){
      logger.info(String.format("sejwowany obiekt %s", o.getClass()));
      return o;
    } else if (countNonEmptyFields.size()>0){
      logger.info(String.format("klasa %s ma %d niepustych pól", o.getClass(), countNonEmptyFields.size()));
      return o;
    } else {
      logger.info(String.format("Klasy %s nie ma na liście, więc jest nullowana.", o.getClass()));
      return null;
    }
  }
}
