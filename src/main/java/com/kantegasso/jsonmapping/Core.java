package com.kantegasso.jsonmapping;

import com.kantegasso.jsonmapping.JsonMapping.JsonMapper;
import com.kantegasso.jsonmapping.JsonMapping.JsonProperty;
import com.kantegasso.jsonmapping.error.JsonMappingException;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

class Core {

  static class Write {

    static <T> Try<JSONObject> objectAsJson(T object, Class<?> valueType, boolean changePrivateModifiersAllowed) {
      return objectAsJson(object, valueType, 0, changePrivateModifiersAllowed);
    }

    static <T> Try<JSONObject> objectAsJson(T object, boolean changePrivateModifiersAllowed) {
      return objectAsJson(object, object.getClass(), 0, changePrivateModifiersAllowed);
    }

    static <T> Try<JSONObject> objectAsJson(T object, Class<?> valueType, int recursionDepth, boolean changePrivateModifiersAllowed) {
      return writeJsonFromAccessors(object, valueType, recursionDepth, changePrivateModifiersAllowed)
          .orElse(writeJsonFromBuilder(object, valueType, recursionDepth, changePrivateModifiersAllowed))
          .orElse(writeJsonFromFields(object, recursionDepth, changePrivateModifiersAllowed));
    }

    static <T> Try<JSONObject> writeJsonFromFields(T object, int recursionDepth, boolean changePrivateModifiersAllowed) {
      return Try.of(() -> object.getClass().getDeclaredFields())
          .mapTry(List::of)
          .filterTry(
              fields -> !fields.isEmpty(),
              () -> new JsonMappingException("JSON-0ZNRDWGTO7", "Instance has no fields"))
          .filterTry(fields -> recursionDepth < Utils.MAX_RECURSION_DEPTH)
          .filterTry(
              _fields -> object.getClass().getDeclaredAnnotation(JsonMapper.class) != null,
              () ->
                  new JsonMappingException(
                      "JSON-LK5AWB99NQ",
                      "The type '"
                          + Try.of(() -> object.getClass().getName()).getOrElse("")
                          + "' does not have the required annotation @JsonMapper."))
          .mapTry(
              fields -> {
                JSONObject jsonObject = new JSONObject();
                fields.forEach(
                    field -> {
                      String fieldName = Utils.parseFieldName(field);
                      Try.of(
                              () -> {
                                if(changePrivateModifiersAllowed) {
                                  Utils.makeFieldAccessible(field);
                                }
                                return field.get(object);
                              })
                          .mapTry(
                              value -> {
                                if (Utils.isBasicJavaObject(value)) {
                                  return value;
                                } else {
                                  return objectAsJson(value, value.getClass(), recursionDepth + 1, changePrivateModifiersAllowed)
                                      .getOrElseThrow(
                                          () ->
                                              new RuntimeException("Failed to write json nested"));
                                }
                              })
                          .mapTry(value -> jsonObject.put(fieldName, value));
                    });
                return jsonObject;
              })
          .filterTry(json -> !json.isEmpty());
    }

    /*
     * Note that @JsonProperty annotations are needed on accessors.
     */
    static <T> Try<JSONObject> writeJsonFromAccessors(
        T object, Class<?> valueType, int recursionDepth, boolean changePrivateModifiersAllowed) {
      return Try.of(() -> valueType)
          .filterTry(_type -> recursionDepth < Utils.MAX_RECURSION_DEPTH)
          .filterTry(
              type -> valueType.getDeclaredAnnotation(JsonMapper.class) != null,
              () ->
                  new JsonMappingException(
                      "JSON-5COOZK00Z8",
                      "The type '"
                          + Try.of(() -> object.getClass().getName()).getOrElse("")
                          + "' does not have the required annotation @JsonMapper."))
          .mapTry(
              type -> {
                JSONObject jsonObject = new JSONObject();
                List.of(type.getDeclaredMethods())
                    .filter(Utils::isMethodAccessor)
                    .filter(accessor -> accessor.getAnnotation(JsonProperty.class) != null)
                    .forEach(
                        accessor -> {
                          String fieldName = Utils.parseFieldName(accessor);
                          Try.of(() -> accessor.invoke(object, (Object[]) null))
                              .mapTry(
                                  value -> {
                                    if (Utils.isBasicJavaObject(value)) {
                                      return value;
                                    } else {
                                      return objectAsJson(
                                          value, value.getClass(), recursionDepth + 1, changePrivateModifiersAllowed);
                                    }
                                  })
                              .mapTry(value -> jsonObject.put(fieldName, value));
                        });
                return jsonObject;
              })
          .filterTry(json -> !json.isEmpty());
    }

  // TODO
  static <T> Try<JSONObject> writeJsonFromBuilder(
        T object, Class<?> valueType, int recursionDepth, boolean changePrivateModifiersAllowed) {
      return Try.of(() -> valueType)
          .filterTry(_type -> recursionDepth < Utils.MAX_RECURSION_DEPTH)
          .filterTry(
              type -> valueType.getDeclaredAnnotation(JsonMapper.class) != null,
              () ->
                  new JsonMappingException(
                      "JSON-5COOZK00Z8",
                      "The type '"
                          + Try.of(() -> object.getClass().getName()).getOrElse("")
                          + "' does not have the required annotation @JsonMapper."))
          .mapTry(
              type -> {
                JSONObject jsonObject = new JSONObject();
                List.of(type.getDeclaredMethods())
                    .filter(Utils::isMethodAccessor)
                    .filter(accessor -> accessor.getAnnotation(JsonProperty.class) != null)
                    .forEach(
                        accessor -> {
                          String fieldName = Utils.parseFieldName(accessor);
                          Try.of(() -> accessor.invoke(object, (Object[]) null))
                              .mapTry(
                                  value -> {
                                    if (Utils.isBasicJavaObject(value)) {
                                      return value;
                                    } else {
                                      return objectAsJson(
                                          value, value.getClass(), recursionDepth + 1, changePrivateModifiersAllowed);
                                    }
                                  })
                              .mapTry(value -> jsonObject.put(fieldName, value));
                        });
                return jsonObject;
              })
          .filterTry(json -> !json.isEmpty());
    }
  }

  static class Read {

    static <T> Try<T> valueFromJson(JSONObject jsonObject, Class<T> valueType, boolean changePrivateModifiersAllowed) {
      return Try.of(() -> valueType)
          .filterTry(
              type -> type.getDeclaredAnnotation(JsonMapper.class) != null,
              () ->
                  new JsonMappingException(
                      "JSON-KFSVGVSYHL",
                      "The type '"
                          + Try.of(valueType::getName).getOrElse("")
                          + "' does not have the required annotation @JsonMapper."))
          .flatMapTry(_type -> valueFromJson(jsonObject, valueType, 0, changePrivateModifiersAllowed));
    }

    private static <T> Try<T> valueFromJson(
        JSONObject jsonObject, Class<T> valueType, int recursionDepth, boolean changePrivateModifiersAllowed) {
      return parseObjectWithConstructor(jsonObject, valueType, recursionDepth, changePrivateModifiersAllowed)
          .orElse(parseMutableObjectWithSetters(jsonObject, valueType, recursionDepth, changePrivateModifiersAllowed))
          .orElse(parseObjectWithFields(jsonObject, valueType, recursionDepth, changePrivateModifiersAllowed));
    }

    static <T> Try<Void> populateInstanceFromJson(
        JSONObject jsonObject, T object, Class<?> valueType, boolean changePrivateModifiersAllowed) {
      return populateInstanceFromJson(jsonObject, object, valueType, 0, changePrivateModifiersAllowed);
    }

    private static <T> Try<Void> populateInstanceFromJson(
        JSONObject jsonObject, T object, Class<?> valueType, int recursionDepth, boolean changePrivateModifiersAllowed) {
      return Try.of(() -> valueType)
          .filterTry(
              type -> type.getDeclaredAnnotation(JsonMapper.class) != null,
              () ->
                  new JsonMappingException(
                      "JSON-Z4R10OE60N",
                      "The type '"
                          + Try.of(valueType::getName).getOrElse("")
                          + "' does not have the required annotation @JsonMapper."))
          .mapTry(
              type ->
                  List.of(type.getDeclaredMethods())
                      .filter(method -> method.getName().startsWith("set"))
                      .filter(method -> method.getParameterCount() == 1)
                      .filter(method -> method.getReturnType().equals(Void.TYPE))
                      .map(setter -> setValueFromJson(object, setter, jsonObject, recursionDepth, changePrivateModifiersAllowed)))
          .filterTry(List::nonEmpty)
          .filterTry(tryList -> tryList.forAll(Try::isSuccess))
          .filterTry(list -> recursionDepth < Utils.MAX_RECURSION_DEPTH)
          .flatMapTry(List::get);
    }

    private static <T> Try<Void> setValueFromJson(
        T object, Method setter, JSONObject jsonObject, int recursionDepth, boolean changePrivateModifiersAllowed) {
      return Try.of(
              () -> {
                Map<String, Object> jsonMap = jsonObject.toMap();
                String fieldName = Utils.parseFieldName(setter);
                return jsonMap.get(fieldName);
              })
          .flatMapTry(
              valueFromJson ->
                  Try.run(
                          () -> {
                            Class<?> setterType = setter.getParameterTypes()[0];
                            Object parsedValue =
                                parseValue(setterType, valueFromJson, recursionDepth, changePrivateModifiersAllowed);
                            setter.invoke(object, parsedValue);
                          })
                      .orElse(
                          Try.run(
                              () -> {
                                if (valueFromJson == null) {
                                  setter.invoke(object, (Object) null);
                                }
                              })));
    }

    private static <T> Try<T> parseMutableObjectWithSetters(
        JSONObject jsonObject, Class<T> valueType, int recursionDepth, boolean changePrivateModifiersAllowed) {
      return Try.of(valueType::newInstance)
          .filterTry(object -> recursionDepth < Utils.MAX_RECURSION_DEPTH)
          .mapTry(
              object -> {
                populateInstanceFromJson(jsonObject, object, valueType, recursionDepth, changePrivateModifiersAllowed)
                    .getOrElseThrow(
                        throwable ->
                            new JsonMappingException(
                                "JSON-8C8T0SOFH8", "Could not parse mutable object. ", throwable));
                return object;
              });
    }

    private static <T> Try<T> parseObjectWithConstructor(
        JSONObject jsonObject, Class<T> valueType, int recursionDepth, boolean changePrivateModifiersAllowed) {
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
          .filterTry(constructor -> recursionDepth < Utils.MAX_RECURSION_DEPTH)
          .mapTry(
              constructor ->
                  constructor.newInstance(sortParameters(jsonObject, constructor, recursionDepth, changePrivateModifiersAllowed)))
          .mapTry(valueType::cast);
    }

    private static Object[] sortParameters(
        JSONObject jsonObject, Constructor<?> constructor, int recursionDepth, boolean changePrivateModifiersAllowed) {
      Map<String, Object> jsonMap = jsonObject.toMap();
      List<Parameter> parameters = List.of(constructor.getParameters());
      return parameters
          .map(
              parameter -> {
                Class<?> parameterType = Utils.convertPrimitiveType(parameter.getType());
                String parameterName =
                    Option.of(parameter.getAnnotation(JsonProperty.class))
                        .map(JsonProperty::value)
                        .filter(value -> !value.isEmpty())
                        .getOrElseThrow(
                            () ->
                                new JsonMappingException(
                                    "JSON-CBVT9U637O",
                                    "JsonProperty annotation in constructor required to construct immutable Java object from json."));
                Object parameterValue =
                    List.ofAll(jsonMap.keySet())
                        .find(
                            jsonKey -> {
                              Class<?> jsonFieldType = jsonMap.get(jsonKey).getClass();
                              return Utils.isTypeApplicableToParse(parameterType, jsonFieldType)
                                  && jsonKey.equals(parameterName);
                            })
                        .map(jsonMap::get)
                        .map(value -> parseValue(parameterType, value, recursionDepth, changePrivateModifiersAllowed))
                        .getOrNull();
                return parameterValue;
              })
          .toJavaArray();
    }

    private static Object parseValue(Class<?> parameterType, Object value, int recursionDepth, boolean changePrivateModifiersAllowed) {
      return Try.of(
              () -> {
                Class<?> valueType = Utils.convertPrimitiveType(value.getClass());
                if (Utils.isTypeEquals(valueType, parameterType)) {
                  return value;
                } else if (parameterType.isEnum() && value instanceof String) {
                  return Try.of(
                          () ->
                              parameterType
                                  .getDeclaredMethod("valueOf", String.class)
                                  .invoke(null, value))
                      .getOrElse(value);
                } else if (Utils.isJavaLangNumber(parameterType)
                    && Utils.isJavaLangNumber(valueType)) {
                  return Try.of(() -> Utils.convertJavaLangNumber(value, valueType, parameterType))
                      .getOrElse(value);
                } else if (value instanceof HashMap) {
                  return Try.of(() -> new JSONObject((HashMap<?, ?>) value))
                      .flatMapTry(json -> valueFromJson(json, parameterType, recursionDepth + 1, changePrivateModifiersAllowed))
                      .getOrElseThrow(
                          () ->
                              new JsonMappingException(
                                  "JSON-A62Z4DGS1R", "Could not parse nested JSON"));
                }
                return value;
              })
          .getOrElse(value);
    }

    private static <T> Try<T> parseObjectWithFields(
        JSONObject jsonObject, Class<T> valueType, int recursionDepth, boolean changePrivateModifiersAllowed) {
      return Try.of(() -> valueType.getDeclaredConstructor().newInstance())
          .filterTry(_instance -> recursionDepth < Utils.MAX_RECURSION_DEPTH)
          .recoverWith(IllegalAccessException.class, Utils.invokePrivateConstuctor(valueType))
          .mapTry(
              instance -> {
                List.of(valueType.getDeclaredFields())
                    .forEach(
                        field -> {
                          String fieldName = Utils.parseFieldName(field);
                          Object valueFromJson = jsonObject.get(fieldName);
                          try {
                            if(changePrivateModifiersAllowed) {
                              Utils.makeFieldModifiable(field);
                            }
                            Class<?> fieldType = Utils.convertPrimitiveType(field.getType());
                            Object parsedValue =
                                parseValue(fieldType, valueFromJson, recursionDepth, changePrivateModifiersAllowed);
                            field.set(instance, parsedValue);
                          } catch (Exception ignored) {
                          }
                        });
                return instance;
              });
    }
  }
}
