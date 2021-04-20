package com.kantegasso.jsonmapping;

import io.vavr.control.Try;
import java.io.File;
import java.io.FileInputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Library for working with <code>org.json</code> objects written in vavr-style. Has both secure
 * wrapping of JSONObject's standard methods for construction and serialization, as well as generic,
 * reflection-based methods.
 */
public class JsonMapping {

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
  public @interface JsonProperty {
    String value() default "";
  }

  public static class Write {
    public static <T> Try<JSONObject> objectAsJson(T object) {
      return Core.Write.objectAsJson(object);
    }

    public static <T> Try<JSONObject> objectAsJson(T object, Class<?> declaredType) {
      return Core.Write.objectAsJson(object);
    }

    public static Try<JSONObject> stringAsJson(String value) {
      return Try.of(() -> new JSONObject(value));
    }

    public static Try<JSONObject> mapAsJson(Map<?, ?> value) {
      return Try.of(() -> new JSONObject(value));
    }
  }

  public static class Read {

    public static <T> Try<T> valueFromJson(JSONObject json, Class<T> valueType) {
      return Core.Read.valueFromJson(json, valueType);
    }

    public static <T> Try<T> valueFromJson(String jsonValue, Class<T> valueType) {
      return Write.stringAsJson(jsonValue)
          .flatMapTry(json -> Core.Read.valueFromJson(json, valueType));
    }

    public static <T> Try<Void> populateInstanceFromJson(JSONObject jsonObject, T object) {
      return Core.Read.populateInstanceFromJson(jsonObject, object, object.getClass());
    }

    public static <T> Try<Void> populateInstanceFromJson(
        JSONObject jsonObject, T object, Class<?> type) {
      return Core.Read.populateInstanceFromJson(jsonObject, object, object.getClass());
    }

    public static Try<Map<String, ?>> mapFromJson(String jsonValue) {
      return Write.stringAsJson(jsonValue).flatMapTry(JsonMapping.Read::mapFromJsonObject);
    }

    public static Try<JSONObject> jsonObjectFromFile(File file) {
      return Try.of(() -> new FileInputStream(file))
          .mapTry(JSONTokener::new)
          .mapTry(JSONObject::new);
    }

    public static Try<Map<String, Object>> objectMapFromJson(String jsonValue) {
      return Write.stringAsJson(jsonValue).flatMapTry(JsonMapping.Read::objectMapFromJson);
    }

    public static Try<Map<String, Object>> objectMapFromJson(JSONObject json) {
      return Try.of(json::toMap);
    }

    public static Try<Map<String, ?>> mapFromJsonObject(JSONObject json) {
      return Try.of(json::toMap);
    }

    public static Try<Map<String, String>> stringMapFromJson(String jsonValue) {
      return Try.of(() -> new JSONObject(jsonValue))
          .flatMapTry(JsonMapping.Read::mapFromJsonObject)
          .mapTry(Utils::convertToStringValues);
    }
  }
}
