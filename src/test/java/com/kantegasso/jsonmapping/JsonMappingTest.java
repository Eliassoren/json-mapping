package com.kantegasso.jsonmapping;

import com.kantegasso.jsonmapping.stub.ApiTokenObject;
import com.kantegasso.jsonmapping.stub.ApiTokenObjectStub;
import com.kantegasso.jsonmapping.stub.ApiTokenObjectStubWithoutAnnotation;
import com.kantegasso.jsonmapping.stub.ApplicationSecretStub;
import com.kantegasso.jsonmapping.stub.Repository;
import com.kantegasso.jsonmapping.stub.ScimTenantConfigStub;
import com.kantegasso.jsonmapping.stub.ScimTenantConfigStub.ScimProviderKind;
import com.kantegasso.jsonmapping.stub.User;
import io.vavr.control.Try;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class JsonMappingTest {
  JsonMapping jsonMapping = new JsonMapping(true);

  @Test
  public void testReadScimTenantConfigFromJson() {

    ScimTenantConfigStub expected =
        new ScimTenantConfigStub(
            "1", "Azure", ApplicationSecretStub.create("a", "b"), ScimProviderKind.AZURE);
    String json = jsonMapping.write.objectAsJson(expected).map(JSONObject::toString).getOrElse("");
    ScimTenantConfigStub maybeActual =
        jsonMapping.read.valueFromJson(json, ScimTenantConfigStub.class).getOrNull();
    Assert.assertEquals(expected, maybeActual);
  }

  @Test
  public void testReadStringMapFromJson() {
    Map<String, String> expected = new HashMap<>();
    expected.put("a", "b");
    expected.put("c", "d");
    String json = jsonMapping.write.mapAsJson(expected).map(JSONObject::toString).getOrElse("");
    Map<String, String> actual = jsonMapping.read.stringMapFromJson(json).getOrNull();
    Assert.assertArrayEquals(expected.keySet().toArray(), actual.keySet().toArray());
    Assert.assertArrayEquals(expected.values().toArray(), actual.values().toArray());
  }

  @Test
  public void testWriteAndReadJsonString() {
    Map<String, String> expected = new HashMap<>();
    expected.put("test", "test");
    String json = "{'test': 'test'}";
    Map<String, String> actual = jsonMapping.read.stringMapFromJson(json).getOrNull();
    Assert.assertArrayEquals(expected.keySet().toArray(), actual.keySet().toArray());
    Assert.assertArrayEquals(expected.values().toArray(), actual.values().toArray());
  }

  @Test
  public void testWriteAndReadCorruptJsonStringMultipleBrackets() {
    Map<String, String> expected = new HashMap<>();
    expected.put("test", "test");
    String json = "{'test': 'test'}}";
    Map<String, String> actual = jsonMapping.read.stringMapFromJson(json).getOrNull();
    Assert.assertArrayEquals(expected.keySet().toArray(), actual.keySet().toArray());
    Assert.assertArrayEquals(expected.values().toArray(), actual.values().toArray());
  }

  @Test
  public void testWriteAndReadCorruptJsonString2() {
    Map<String, String> expected = new HashMap<>();
    expected.put("test", "test");
    String json = "{'test': 'test'}}";
    Map<String, String> actual = jsonMapping.read.stringMapFromJson(json).getOrNull();
    Assert.assertArrayEquals(expected.keySet().toArray(), actual.keySet().toArray());
    Assert.assertArrayEquals(expected.values().toArray(), actual.values().toArray());
  }

  @Test
  public void testWriteAndReadNestedJson() {
    Map<String, HashMap<String, String>> expected = new HashMap<>();
    expected.put("test", io.vavr.collection.HashMap.of("a", "b").toJavaMap());
    String json = "{'test': {'a': 'b'}}";

    @SuppressWarnings("unchecked")
    Map<String, HashMap<String, String>> actual =
        Try.of(
                () ->
                    (Map<String, HashMap<String, String>>)
                        jsonMapping.read.mapFromJson(json).getOrNull())
            .getOrElse(new HashMap<>());
    Assert.assertArrayEquals(expected.keySet().toArray(), actual.keySet().toArray());
    Assert.assertArrayEquals(expected.values().toArray(), actual.values().toArray());
  }

  @Test
  public void testReadJsonWithArray() {
    Map<String, List<HashMap<String, String>>> expected = new HashMap<>();
    expected.put(
        "test", Collections.singletonList(io.vavr.collection.HashMap.of("a", "b").toJavaMap()));
    String json = "{'test': [{'a': 'b'}]}";

    Map<String, Object> actual = jsonMapping.read.objectMapFromJson(json).getOrNull();
    Assert.assertArrayEquals(actual.keySet().toArray(), expected.keySet().toArray());
    Assert.assertArrayEquals(actual.values().toArray(), expected.values().toArray());
  }

  @Test
  public void testGenericReadApiTokenFromJson() {
    ApiTokenObjectStub expected = new ApiTokenObjectStub();
    expected.setAlias("alias");
    expected.setCreatedAt(1);
    expected.setHashed("hashed");
    expected.setSalt("salt");
    expected.setUserKey("userkey");
    expected.setValidFor(1);
    String json = jsonMapping.write.objectAsJson(expected).map(JSONObject::toString).getOrElse("");
    ApiTokenObject maybeActual =
        jsonMapping.read.valueFromJson(json, ApiTokenObjectStub.class).getOrNull();
    Assert.assertEquals(expected, maybeActual);
  }

  @Test
  public void testGenericReadApiTokenFromJsonWithoutInstanceFails() {
    ApiTokenObject expected = new ApiTokenObjectStub();
    expected.setAlias("alias");
    expected.setCreatedAt(1);
    expected.setHashed("hashed");
    expected.setSalt("salt");
    expected.setUserKey("userkey");
    expected.setValidFor(1);
    String json = jsonMapping.write.objectAsJson(expected).map(JSONObject::toString).getOrElse("");
    Try<ApiTokenObject> maybeActual = jsonMapping.read.valueFromJson(json, ApiTokenObject.class);
    Assert.assertTrue(maybeActual.isFailure()); // Fail because interface does not have constructor
  }

  @Test
  public void testGenericReadApiTokenFromJsonWithoutAnnotation() {
    ApiTokenObjectStubWithoutAnnotation expected = new ApiTokenObjectStubWithoutAnnotation();
    expected.setAlias("alias");
    expected.setCreatedAt(1);
    expected.setHashed("hashed");
    expected.setSalt("salt");
    expected.setUserKey("userkey");
    expected.setValidFor(1);
    Try<String> maybeJson = jsonMapping.write.objectAsJson(expected).map(JSONObject::toString);
    Try<ApiTokenObjectStubWithoutAnnotation> maybeActual =
        jsonMapping.read.valueFromJson(
            maybeJson.getOrElse(""), ApiTokenObjectStubWithoutAnnotation.class);
    Assert.assertNull(maybeActual.getOrNull());
    Assert.assertEquals("", maybeJson.getOrElse(""));
  }

  @Test
  public void testPopulateApiTokenFromJson() {
    ApiTokenObjectStub expected = new ApiTokenObjectStub();
    expected.setAlias("alias");
    expected.setCreatedAt(3);
    expected.setHashed("hashed");
    expected.setSalt("salt");
    expected.setUserKey("userkey");
    expected.setValidFor(1);
    JSONObject json = jsonMapping.write.objectAsJson(expected, ApiTokenObject.class).getOrNull();
    ApiTokenObjectStub actual = new ApiTokenObjectStub();
    jsonMapping.read.populateInstanceFromJson(json, actual, ApiTokenObject.class);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testPopulateApiTokenFromJsonInterface() {
    ApiTokenObject expected = new ApiTokenObjectStub();
    expected.setAlias("alias");
    expected.setCreatedAt(3);
    expected.setHashed("hashed");
    expected.setSalt("salt");
    expected.setUserKey("userkey");
    expected.setValidFor(1);
    JSONObject json = jsonMapping.write.objectAsJson(expected, ApiTokenObject.class).getOrNull();
    ApiTokenObject actual = new ApiTokenObjectStub();
    jsonMapping.read.populateInstanceFromJson(json, actual, ApiTokenObject.class);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testPopulateApiTokenFromJsonInterfaceAccessors() {
    ApiTokenObject expected = new ApiTokenObjectStub();
    expected.setAlias("alias");
    expected.setCreatedAt(3);
    expected.setHashed("hashed");
    expected.setSalt("salt");
    expected.setUserKey("userkey");
    expected.setValidFor(1);
    JSONObject json =
        Core.Write.writeJsonFromAccessors(expected, ApiTokenObject.class, 0, false).getOrNull();
    ApiTokenObject actual = new ApiTokenObjectStub();
    jsonMapping.read.populateInstanceFromJson(json, actual, ApiTokenObject.class);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testWriteDummyUserFromJson() {
    Repository repository = new Repository();
    User user = repository.createUser(); // ID = 8777
    user.setUsername("jondoe");
    user.setEmail("jondoe@example.com");
    List<String> groups = Arrays.asList("group 1", "group 2");
    user.setGroups(groups);
    User user2 = repository.createUser(); // ID = 8777
    user2.setUsername("jondoe2");
    user2.setEmail("jondoe2@example.com");
    user2.setGroups(Arrays.asList("group 1", "group 2"));
    User user3 = repository.createUser(); // ID = 8777
    user3.setUsername("jondoe3");
    user3.setEmail("jondoe2@example.com");
    user3.setGroups(Arrays.asList("group 1", "group 2"));
    user.setContacts(Arrays.asList(user2, user3));
    user3.setContacts(Arrays.asList(user, user2));
    JSONObject json = jsonMapping.write.objectAsJson(user).getOrNull();
    /* json:
        {
            "ID": 8777,
            "username": "jondoe",
            "email": "jondoe@example.com",
            "groups": ["group 1", "group 2"]
        }
    */
    User actual = jsonMapping.read.valueFromJson(json, User.class).getOrNull();
    Assert.assertEquals(user, actual); // true
  }
}
