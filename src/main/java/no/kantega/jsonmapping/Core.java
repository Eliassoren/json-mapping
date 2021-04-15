package no.kantega.jsonmapping;

import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

class Core {

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
  public @interface JsonProperty {
    String value() default "";
  }

  private static final int MAX_RECURSION_DEPTH = 4;

  private static final Map<Class<?>, Class<?>> primitiveTypeConversion = new HashMap<>();

  static {
    primitiveTypeConversion.put(boolean.class, Boolean.class);
    primitiveTypeConversion.put(byte.class, Byte.class);
    primitiveTypeConversion.put(short.class, Short.class);
    primitiveTypeConversion.put(char.class, Character.class);
    primitiveTypeConversion.put(int.class, Integer.class);
    primitiveTypeConversion.put(long.class, Long.class);
    primitiveTypeConversion.put(float.class, Float.class);
    primitiveTypeConversion.put(double.class, Double.class);
  }

  private static String asField(String methodName) {
    List<String> possiblePrefixes = List.of("is", "get", "set");
    String methodPrefix = possiblePrefixes.filter(methodName::startsWith).get();
    String fieldName = methodName.replaceFirst(methodPrefix, "");
    return String.format(
        "%s%s", Character.toLowerCase(fieldName.charAt(0)), fieldName.substring(1));
  }

  private static String accessor(Field field) {
    Class<?> type = convertPrimitiveType(field.getType());
    if (type.equals(Boolean.class)) {
      return booleanAccessor(field.getName());
    }
    return getterAccessor(field.getName());
  }

  private static String booleanAccessor(String fieldName) {
    return String.format(
        "is%s%s", Character.toUpperCase(fieldName.charAt(0)), fieldName.substring(1));
  }

  private static boolean isMethodAccessor(Method method) {
    return Try.of(() -> method.getName().startsWith("get") || method.getName().startsWith("is"))
        .getOrElse(false);
  }

  private static String getterAccessor(String fieldName) {
    return String.format(
        "get%s%s", Character.toUpperCase(fieldName.charAt(0)), fieldName.substring(1));
  }

  private static String setterAccessor(String fieldName) {
    return String.format(
        "set%s%s", Character.toUpperCase(fieldName.charAt(0)), fieldName.substring(1));
  }

  private static Class<?> convertPrimitiveType(Class<?> type) {
    return Try.of(() -> type.isPrimitive() ? primitiveTypeConversion.get(type) : type)
        .getOrElse(type);
  }

  // For converting between java.lang.Integer, java.lang.Long, etc.
  private static boolean isJavaLangNumber(Class<?> type) {
    return Try.of(() -> convertPrimitiveType(type))
        .mapTry(objectType -> objectType.getSuperclass().equals(Number.class))
        .getOrElse(false);
  }

  private static boolean isTypeEquals(Class<?> type1, Class<?> type2) {
    return convertPrimitiveType(type1).equals(convertPrimitiveType(type2));
  }

  private static boolean isJavaLangObject(Class<?> type) {
    return type.getPackage().getName().startsWith("java.lang");
  }

  private static boolean isBasicJavaObject(Object value) {
    return value.getClass().isPrimitive()
        || value.getClass().getPackage().getName().startsWith("java")
        || value.getClass().isEnum();
  }

  static class Read {
    static <T> Try<T> valueFromJson(JSONObject jsonObject, Class<T> valueType) {
      return valueFromJson(jsonObject, valueType, 0);
    }

    private static <T> Try<T> valueFromJson(
      JSONObject jsonObject, Class<T> valueType, int recursionDepth) {
      return parseObjectWithConstructor(jsonObject, valueType, recursionDepth)
        .orElse(parseMutableObjectWithSetters(jsonObject, valueType, recursionDepth))
        .orElse(parseObjectWithFields(jsonObject, valueType, recursionDepth));
    }

    static <T> Try<Void> populateInstanceFromJson(JSONObject jsonObject, T object) {
      return populateInstanceFromJson(jsonObject, object, 0);
    }

    private static <T> Try<Void> populateInstanceFromJson(JSONObject jsonObject, T object, int recursionDepth) {
      return Try.of(
              () ->
                  List.of(object.getClass().getDeclaredMethods())
                      .filter(method -> method.getName().startsWith("set"))
                      .filter(method -> method.getParameterCount() == 1)
                      .filter(method -> method.getReturnType().equals(Void.TYPE))
                      .map(setter -> setValueFromJson(object, setter, jsonObject, recursionDepth)))
          .filterTry(List::nonEmpty)
          .filterTry(tryList -> tryList.forAll(Try::isSuccess))
          .filterTry(list -> recursionDepth < MAX_RECURSION_DEPTH)
          .flatMapTry(List::get);
    }

    private static <T> Try<Void> setValueFromJson(
        T object, Method setter, JSONObject jsonObject, int recursionDepth) {
      return Try.of(
              () -> {
                Map<String, Object> jsonMap = jsonObject.toMap();
                String fieldName =
                    Option.of(setter.getAnnotation(JsonProperty.class))
                        .map(JsonProperty::value)
                        .filter(value -> !value.isEmpty())
                        .getOrElse(asField(setter.getName()));
                return jsonMap.get(fieldName);
              })
          .flatMapTry(
              valueFromJson ->
                  Try.run(
                          () -> {
                            Class<?> setterType =
                                convertPrimitiveType(setter.getParameterTypes()[0]);
                            Object parsedValue =
                                parseValue(setterType, valueFromJson, recursionDepth);
                            setter.invoke(object, parsedValue);
                          })
                      .orElse(
                          Try.run(
                              () -> {
                                if (valueFromJson == null) {
                                  Class<?> setterType =
                                      convertPrimitiveType(setter.getParameterTypes()[0]);
                                  setter.invoke(setterType, (Object) null);
                                }
                              })));
    }

    private static Object convertJavaLangNumber(
        Object value, Class<?> valueType, Class<?> typeToConvert)
        throws IllegalAccessException, InvocationTargetException {
      // Handle problem with conversion between java.lang - Integer, Long etc.
      Method valueConversionMethod =
          List.of(valueType.getMethods())
              .filter(method -> isTypeEquals(typeToConvert, method.getReturnType()))
              .filter(method -> method.getName().endsWith("Value"))
              .get(); // for example long Integer.longValue()
      return valueConversionMethod.invoke(value);
    }

    private static <T> Try<T> parseMutableObjectWithSetters(
        JSONObject jsonObject, Class<T> valueType, int recursionDepth) {
      return Try.of(valueType::newInstance)
          .filterTry(object -> recursionDepth < MAX_RECURSION_DEPTH)
          .filterTry(object -> populateInstanceFromJson(jsonObject, object, recursionDepth).isSuccess());
    }

    private static <T> Try<T> parseObjectWithConstructor(
        JSONObject jsonObject, Class<T> valueType, int recursionDepth) {
      return Try.of(
              () -> {
                List<Class<?>> fieldTypes =
                    List.of(valueType.getDeclaredFields())
                        .filter(field -> !field.getName().startsWith("this$"))
                        .map(Field::getType);
                return List.of(valueType.getConstructors())
                    .filter(
                        constructor ->
                            List.of(constructor.getParameterTypes()).containsAll(fieldTypes))
                    .get();
              })
          .mapTry(
              constructor -> {
                constructor.setAccessible(true);
                return constructor;
              })
          .filterTry(constructor -> recursionDepth < MAX_RECURSION_DEPTH)
          .mapTry(
              constructor ->
                  constructor.newInstance(sortParameters(jsonObject, constructor, recursionDepth)))
          .mapTry(valueType::cast);
    }

    private static Object[] sortParameters(
        JSONObject jsonObject, Constructor<?> constructor, int recursionDepth) {
      Map<String, Object> jsonMap = jsonObject.toMap();
      List<Parameter> parameters = List.of(constructor.getParameters());
      return parameters
          .map(
              parameter -> {
                Class<?> parameterType = convertPrimitiveType(parameter.getType());
                String parameterName =
                    Option.of(parameter.getAnnotation(JsonProperty.class))
                        .map(JsonProperty::value)
                        .filter(value -> !value.isEmpty())
                        .getOrElseThrow(
                            () ->
                                new RuntimeException(
                                    "JsonProperty annotation in constructor needed to construct immutable Java object from json."));
                Object parameterValue =
                    List.ofAll(jsonMap.keySet())
                        .find(
                            jsonKey -> {
                              Class<?> jsonFieldType = jsonMap.get(jsonKey).getClass();
                              return isTypeApplicableToParameter(parameterType, jsonFieldType)
                                  && jsonKey.equals(parameterName);
                            })
                        .map(jsonMap::get)
                        .map(value -> parseValue(parameterType, value, recursionDepth))
                        .getOrNull();
                return parameterValue;
              })
          .toJavaArray();
    }

    private static Object parseValue(Class<?> parameterType, Object value, int recursionDepth) {
      return Try.of(
              () -> {
                Class<?> valueType = convertPrimitiveType(value.getClass());
                if (isTypeEquals(valueType, parameterType)) {
                  return value;
                } else if (parameterType.isEnum() && value instanceof String) {
                  return Try.of(
                          () ->
                              parameterType
                                  .getDeclaredMethod("valueOf", String.class)
                                  .invoke(null, value))
                      .getOrElse(value);
                } else if (isJavaLangNumber(parameterType) && isJavaLangNumber(valueType)) {
                  return Try.of(() -> convertJavaLangNumber(value, valueType, parameterType))
                      .getOrElse(value);
                } else if (value instanceof HashMap) {
                  return JsonMapping.Write.mapAsJson((HashMap<?, ?>) value)
                      .flatMapTry(json -> valueFromJson(json, parameterType, recursionDepth + 1))
                      .getOrElseThrow(() -> new RuntimeException("Could not parse nested JSON"));
                }
                return value;
              })
          .getOrElse(value);
    }

    private static boolean isTypeApplicableToParameter(
        Class<?> parameterType, Class<?> jsonFieldType) {
      return isTypeEquals(jsonFieldType, parameterType)
          || (isJavaLangNumber(jsonFieldType) && isJavaLangNumber(parameterType))
          || (parameterType.isEnum() && jsonFieldType.equals(String.class))
          || jsonFieldType.equals(HashMap.class);
    }

    private static <T> Try<T> parseObjectWithFields(
        JSONObject jsonObject, Class<T> valueType, int recursionDepth) {
      return Try.of(() -> valueType.newInstance())
          .filterTry(_instance -> recursionDepth < MAX_RECURSION_DEPTH)
          .mapTry(
              instance -> {
                List.of(valueType.getDeclaredFields())
                    .forEach(
                        field -> {
                          String fieldName = getFieldName(field);
                          Object valueFromJson = jsonObject.get(fieldName);
                          try {
                            makeFieldAccessibleAndModifiable(field);
                            Class<?> fieldType = convertPrimitiveType(field.getType());
                            Object parsedValue = parseValue(fieldType, valueFromJson, recursionDepth);
                            field.set(instance, parsedValue);
                          } catch (Exception ignored) {
                          }
                        });
                return instance;
              });
    }

    private static String getFieldName(Field field) {
      return Option.of(field.getAnnotation(JsonProperty.class))
          .map(JsonProperty::value)
          .filter(value -> !value.isEmpty())
          .getOrElse(field.getName());
    }

    private static void makeFieldAccessibleAndModifiable(Field field)
        throws NoSuchFieldException, IllegalAccessException {
      field.setAccessible(true);
      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }
  }

  static class Write {

    static <T> Try<JSONObject> objectAsJson(T object) {
      return objectAsJson(object, 0);
    }

    static <T> Try<JSONObject> objectAsJson(T object, int recursionDepth) {
      return writeJsonFromAccessors(object, recursionDepth)
          .orElse(writeJsonFromFields(object, recursionDepth));
    }

    private static <T> Try<JSONObject> writeJsonFromFields(T object, int recursionDepth) {
      return Try.of(() -> object.getClass().getDeclaredFields())
          .mapTry(List::of)
          .filterTry(
              fields -> !fields.isEmpty(), () -> new RuntimeException("Instance has no fields"))
          .filterTry(fields -> recursionDepth < MAX_RECURSION_DEPTH)
          .mapTry(
              fields -> {
                JSONObject jsonObject = new JSONObject();
                fields.forEach(
                    field -> {
                      String fieldName =
                          Option.of(field.getAnnotation(JsonProperty.class))
                              .filter(property -> !property.value().isEmpty())
                              .map(JsonProperty::value)
                              .getOrElse(field.getName());
                      Try.of(
                              () -> {
                                field.setAccessible(true);
                                return field.get(object);
                              })
                          .mapTry(
                              value -> {
                                if (isBasicJavaObject(value)) {
                                  return value;
                                } else {
                                  return objectAsJson(value, recursionDepth + 1)
                                      .getOrElseThrow(
                                          () ->
                                              new RuntimeException("Failed to write json nested"));
                                }
                              })
                          .mapTry(value -> jsonObject.put(fieldName, value));
                    });
                return jsonObject;
              });
    }

    /*
     * Note that @JsonProperty annotations are needed on accessors.
     */
    private static <T> Try<JSONObject> writeJsonFromAccessors(T object, int recursionDepth) {
      return Try.of(() -> object.getClass())
          .filterTry(_valueType -> recursionDepth < MAX_RECURSION_DEPTH)
          .mapTry(
              valueType -> {
                JSONObject jsonObject = new JSONObject();
                List.of(valueType.getDeclaredMethods())
                    .filter(Core::isMethodAccessor)
                    .filter(accessor -> accessor.getAnnotation(JsonProperty.class) != null)
                    .forEach(
                        accessor -> {
                          String fieldName =
                              Option.of(accessor.getAnnotation(JsonProperty.class))
                                  .filter(property -> !property.value().isEmpty())
                                  .map(JsonProperty::value)
                                  .getOrElse(asField(accessor.getName()));
                          Try.of(() -> accessor.invoke(object, valueType))
                              .mapTry(
                                  value -> {
                                    if (isBasicJavaObject(value)) {
                                      return value;
                                    } else {
                                      return objectAsJson(value, recursionDepth + 1);
                                    }
                                  })
                              .mapTry(value -> jsonObject.put(fieldName, value));
                        });
                return jsonObject;
              })
          .filterTry(json -> !json.isEmpty());
    }
  }
}
