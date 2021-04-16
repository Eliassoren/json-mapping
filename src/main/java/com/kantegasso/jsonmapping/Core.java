package com.kantegasso.jsonmapping;

import com.kantegasso.jsonmapping.JsonMapping.JsonProperty;
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
          .filterTry(fields -> recursionDepth < Utils.MAX_RECURSION_DEPTH)
          .mapTry(
              fields -> {
                JSONObject jsonObject = new JSONObject();
                fields.forEach(
                    field -> {
                      String fieldName = Utils.parseFieldName(field);
                      Try.of(
                              () -> {
                                Utils.makeFieldAccessible(field);
                                return field.get(object);
                              })
                          .mapTry(
                              value -> {
                                if (Utils.isBasicJavaObject(value)) {
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
              })
          .filterTry(json -> !json.isEmpty());
    }

    /*
     * Note that @JsonProperty annotations are needed on accessors.
     */
    private static <T> Try<JSONObject> writeJsonFromAccessors(T object, int recursionDepth) {
      return Try.of(() -> object.getClass())
          .filterTry(_valueType -> recursionDepth < Utils.MAX_RECURSION_DEPTH)
          .mapTry(
              valueType -> {
                JSONObject jsonObject = new JSONObject();
                List.of(valueType.getDeclaredMethods())
                    .filter(Utils::isMethodAccessor)
                    .filter(accessor -> accessor.getAnnotation(JsonProperty.class) != null)
                    .forEach(
                        accessor -> {
                          String fieldName = Utils.parseFieldName(accessor);
                          Try.of(() -> accessor.invoke(object, valueType))
                              .mapTry(
                                  value -> {
                                    if (Utils.isBasicJavaObject(value)) {
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

    private static <T> Try<Void> populateInstanceFromJson(
        JSONObject jsonObject, T object, int recursionDepth) {
      return Try.of(
              () ->
                  List.of(object.getClass().getDeclaredMethods())
                      .filter(method -> method.getName().startsWith("set"))
                      .filter(method -> method.getParameterCount() == 1)
                      .filter(method -> method.getReturnType().equals(Void.TYPE))
                      .map(setter -> setValueFromJson(object, setter, jsonObject, recursionDepth)))
          .filterTry(List::nonEmpty)
          .filterTry(tryList -> tryList.forAll(Try::isSuccess))
          .filterTry(list -> recursionDepth < Utils.MAX_RECURSION_DEPTH)
          .flatMapTry(List::get);
    }

    private static <T> Try<Void> setValueFromJson(
        T object, Method setter, JSONObject jsonObject, int recursionDepth) {
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
                            Class<?> setterType =
                                Utils.convertPrimitiveType(setter.getParameterTypes()[0]);
                            Object parsedValue =
                                parseValue(setterType, valueFromJson, recursionDepth);
                            setter.invoke(object, parsedValue);
                          })
                      .orElse(
                          Try.run(
                              () -> {
                                if (valueFromJson == null) {
                                  Class<?> setterType =
                                      Utils.convertPrimitiveType(setter.getParameterTypes()[0]);
                                  setter.invoke(setterType, (Object) null);
                                }
                              })));
    }

    private static <T> Try<T> parseMutableObjectWithSetters(
        JSONObject jsonObject, Class<T> valueType, int recursionDepth) {
      return Try.of(valueType::newInstance)
          .filterTry(object -> recursionDepth < Utils.MAX_RECURSION_DEPTH)
          .filterTry(
              object -> populateInstanceFromJson(jsonObject, object, recursionDepth).isSuccess());
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
          .filterTry(constructor -> recursionDepth < Utils.MAX_RECURSION_DEPTH)
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
                Class<?> parameterType = Utils.convertPrimitiveType(parameter.getType());
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
                              return Utils.isTypeApplicableToParse(parameterType, jsonFieldType)
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
                  return JsonMapping.Write.mapAsJson((HashMap<?, ?>) value)
                      .flatMapTry(json -> valueFromJson(json, parameterType, recursionDepth + 1))
                      .getOrElseThrow(() -> new RuntimeException("Could not parse nested JSON"));
                }
                return value;
              })
          .getOrElse(value);
    }

    private static <T> Try<T> parseObjectWithFields(
        JSONObject jsonObject, Class<T> valueType, int recursionDepth) {
      return Try.of(() -> valueType.newInstance())
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
                            Utils.makeFieldModifiable(field);
                            Class<?> fieldType = Utils.convertPrimitiveType(field.getType());
                            Object parsedValue =
                                parseValue(fieldType, valueFromJson, recursionDepth);
                            field.set(instance, parsedValue);
                          } catch (Exception ignored) {
                          }
                        });
                return instance;
              });
    }
  }
}
