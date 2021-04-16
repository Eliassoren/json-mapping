package com.kantegasso.jsonmapping;

import com.kantegasso.jsonmapping.JsonMapping.JsonProperty;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

class Utils {

  static final int MAX_RECURSION_DEPTH = 10;

  static final Map<Class<?>, Class<?>> primitiveTypeConversion = new HashMap<>();

  static {
    Utils.primitiveTypeConversion.put(boolean.class, Boolean.class);
    Utils.primitiveTypeConversion.put(byte.class, Byte.class);
    Utils.primitiveTypeConversion.put(short.class, Short.class);
    Utils.primitiveTypeConversion.put(char.class, Character.class);
    Utils.primitiveTypeConversion.put(int.class, Integer.class);
    Utils.primitiveTypeConversion.put(long.class, Long.class);
    Utils.primitiveTypeConversion.put(float.class, Float.class);
    Utils.primitiveTypeConversion.put(double.class, Double.class);
  }

  static String asField(String methodName) {
    List<String> possiblePrefixes = List.of("is", "get", "set");
    String methodPrefix = possiblePrefixes.filter(methodName::startsWith).get();
    String fieldName = methodName.replaceFirst(methodPrefix, "");
    return String.format(
        "%s%s", Character.toLowerCase(fieldName.charAt(0)), fieldName.substring(1));
  }

  static boolean isMethodAccessor(Method method) {
    return Try.of(() -> method.getName().startsWith("get") || method.getName().startsWith("is"))
        .getOrElse(false);
  }

  static Class<?> convertPrimitiveType(Class<?> type) {
    return Try.of(() -> type.isPrimitive() ? primitiveTypeConversion.get(type) : type)
        .getOrElse(type);
  }

  // For converting between java.lang.Integer, java.lang.Long, etc.
  static boolean isJavaLangNumber(Class<?> type) {
    return Try.of(() -> convertPrimitiveType(type))
        .mapTry(objectType -> objectType.getSuperclass().equals(Number.class))
        .getOrElse(false);
  }

  static boolean isTypeEquals(Class<?> type1, Class<?> type2) {
    return convertPrimitiveType(type1).equals(convertPrimitiveType(type2));
  }

  static boolean isBasicJavaObject(Object value) {
    return isBasicJavaObject(value.getClass());
  }

  static boolean isBasicJavaObject(Class<?> type) {
    return type.isPrimitive() || type.getPackage().getName().startsWith("java") || type.isEnum();
  }

  static Object convertJavaLangNumber(Object value, Class<?> valueType, Class<?> typeToConvert)
      throws IllegalAccessException, InvocationTargetException {
    // Handle problem with conversion between java.lang - Integer, Long etc.
    Method valueConversionMethod =
        List.of(valueType.getMethods())
            .filter(method -> isTypeEquals(typeToConvert, method.getReturnType()))
            .filter(method -> method.getName().endsWith("Value"))
            .get(); // for example long Integer.longValue()
    return valueConversionMethod.invoke(value);
  }

  static boolean isTypeApplicableToParse(Class<?> typeTo, Class<?> typeFrom) {
    return isTypeEquals(typeFrom, typeTo)
        || (isJavaLangNumber(typeFrom) && isJavaLangNumber(typeTo))
        || (typeFrom.equals(String.class) && typeTo.isEnum())
        || (typeFrom.equals(HashMap.class) && !isBasicJavaObject(typeTo));
  }

  static String getFieldName(Field field) {
    return Option.of(field.getAnnotation(JsonProperty.class))
        .map(JsonProperty::value)
        .filter(value -> !value.isEmpty())
        .getOrElse(field.getName());
  }

  static void makeFieldModifiable(Field field) {
    AccessController.doPrivileged(
        (PrivilegedAction<Object>)
            () -> {
              try {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                makeFieldAccessible(field);
                makeFieldAccessible(modifiersField);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
              } catch (IllegalAccessException | NoSuchFieldException ignored) {
              }
              return null;
            });
  }

  static void makeFieldAccessible(Field field) {
    AccessController.doPrivileged(
        (PrivilegedAction<Object>)
            () -> {
              try {
                field.setAccessible(true);
              } catch (SecurityException ignored) {
              }
              return null;
            });
  }

  static Map<String, String> convertToStringValues(Map<String, ?> map) {
    Map<String, String> newMap = new HashMap<>();
    map.forEach(
        (key, value) -> {
          newMap.put(key, String.valueOf(value));
        });
    return newMap;
  }

  static <T> Try<T> invokePrivateConstuctor(Class<T> valueType) {
    return Try.of(
            () ->
                List.of(valueType.getDeclaredConstructors())
                    .filter(constructor -> constructor.getParameterCount() == 0)
                    .map(
                        constructor -> {
                          constructor.setAccessible(true);
                          return constructor;
                        })
                    .get()
                    .newInstance())
        .mapTry(valueType::cast);
  }
}
