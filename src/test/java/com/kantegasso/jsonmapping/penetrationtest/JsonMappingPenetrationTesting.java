package com.kantegasso.jsonmapping.penetrationtest;

import com.kantegasso.jsonmapping.JsonMapping;
import io.vavr.control.Try;
import java.io.IOException;
import org.json.JSONObject;

public class JsonMappingPenetrationTesting {
  public static void testInsecureDeseralization() throws IOException {
    JSONObject json = new JSONObject();
    boolean isWindows = true;
    String spelLocation =
        isWindows
            ? "C:\\Users\\elisor\\Documents\\git\\ksso\\json-mapping\\src\\test\\java\\com\\kantegasso\\jsonmapping\\penetrationtest\\spel-windows.xml"
            : "Paths. test\\java\\com\\kantegasso\\jsonmapping\\penetrationtest\\spel-mac.xml";
    // Note: this only works with the DefaultTyping feature in Jackson,
    // where an array with type as the first arg and constructor
    // args as the following is specified as string. That is not possible with this lib, so we are
    // relatively safe. Also demanding the @JsonMapper annotation helps security.
    json.put(
        "name",
        new Object[] {
          "org.springframework.context.support.FileSystemXmlApplicationContext", spelLocation
        });
    json.put("age", Integer.MAX_VALUE);
    Try<InsecureObject> a = JsonMapping.Read.valueFromJson(json, InsecureObject.class);
    a.peek(
        value -> {
          Object d = (value.getName());
          System.out.println("a" + value.getAge());
        });
  }

  public static void main(String[] args) throws IOException {
    testInsecureDeseralization();
  }
}
