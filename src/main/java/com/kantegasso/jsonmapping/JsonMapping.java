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
 * reflection-based methods. Using thread-safety and runtime safety of VAVR's Try.
 */
public class JsonMapping {

  private boolean changePrivateModifiersAllowed = false;
  public final Write write = new Write();
  public final Read read = new Read();
  public JsonMapping() { }

  public JsonMapping(boolean changePrivateModifiersAllowed) {
    this.changePrivateModifiersAllowed = changePrivateModifiersAllowed;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
  public @interface JsonProperty {
    String value() default "";
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE})
  public @interface JsonMapper {}

  public class Write {
    public Write() {}

    public <T> Try<JSONObject> objectAsJson(T object) {
      return Core.Write.objectAsJson(object, changePrivateModifiersAllowed);
    }

    public <T> Try<JSONObject> objectAsJson(T object, Class<?> declaredType) {
      return Core.Write.objectAsJson(object, declaredType, changePrivateModifiersAllowed);
    }

    public Try<JSONObject> stringAsJson(String value) {
      return Try.of(() -> new JSONObject(value));
    }

    public Try<JSONObject> mapAsJson(Map<?, ?> value) {
      return Try.of(() -> new JSONObject(value));
    }

    public <T> Try<String> objectAsJsonString(T object) {
      return Core.Write.objectAsJson(object, changePrivateModifiersAllowed)
        .mapTry(JSONObject::toString);
    }

    public <T> Try<String> objectAsJsonString(T object, Class<?> declaredType) {
      return Core.Write.objectAsJson(object, declaredType, changePrivateModifiersAllowed)
        .mapTry(JSONObject::toString);
    }

    public Try<String> mapAsJsonString(Map<?, ?> value) {
      return Try.of(() -> new JSONObject(value))
        .mapTry(JSONObject::toString);
    }
  }

  public class Read {

    public Read() {}
    public <T> Try<T> valueFromJson(JSONObject json, Class<T> valueType) {
      return Core.Read.valueFromJson(json, valueType, changePrivateModifiersAllowed);
    }

    public <T> Try<T> valueFromJson(String jsonValue, Class<T> valueType) {
      return write.stringAsJson(jsonValue)
          .flatMapTry(json -> Core.Read.valueFromJson(json, valueType, changePrivateModifiersAllowed));
    }

    public <T> Try<Void> populateInstanceFromJson(JSONObject jsonObject, T object) {
      return Core.Read.populateInstanceFromJson(jsonObject, object, object.getClass(), changePrivateModifiersAllowed);
    }

    public <T> Try<Void> populateInstanceFromJson(
        JSONObject jsonObject, T object, Class<?> type) {
      return Core.Read.populateInstanceFromJson(jsonObject, object, type, changePrivateModifiersAllowed);
    }

    public Try<Map<String, ?>> mapFromJson(String jsonValue) {
      return write.stringAsJson(jsonValue).flatMapTry(this::mapFromJsonObject);
    }

    public Try<JSONObject> jsonObjectFromFile(File file) {
      return Try.of(() -> new FileInputStream(file))
          .mapTry(JSONTokener::new)
          .mapTry(JSONObject::new);
    }

    public Try<Map<String, Object>> objectMapFromJson(String jsonValue) {
      return write.stringAsJson(jsonValue).flatMapTry(this::objectMapFromJson);
    }

    public Try<Map<String, Object>> objectMapFromJson(JSONObject json) {
      return Try.of(json::toMap);
    }

    public Try<Map<String, ?>> mapFromJsonObject(JSONObject json) {
      return Try.of(json::toMap);
    }

    public Try<Map<String, String>> stringMapFromJson(String jsonValue) {
      return Try.of(() -> new JSONObject(jsonValue))
          .flatMapTry(this::mapFromJsonObject)
          .mapTry(Utils::convertToStringValues);
    }
  }
}
