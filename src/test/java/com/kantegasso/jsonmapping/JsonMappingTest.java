package com.kantegasso.jsonmapping;

import com.kantegasso.jsonmapping.stub.ApiTokenObjectStub;
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

  @Test
  public void testReadScimTenantConfigFromJson() {

    ScimTenantConfigStub expected =
        new ScimTenantConfigStub(
            "1", "Azure", ApplicationSecretStub.create("a", "b"), ScimProviderKind.AZURE);
    String json = JsonMapping.Write.objectAsJson(expected).map(JSONObject::toString).getOrElse("");
    ScimTenantConfigStub maybeActual =
        JsonMapping.Read.valueFromJson(json, ScimTenantConfigStub.class).getOrNull();
    Assert.assertEquals(expected, maybeActual);
  }

  @Test
  public void testReadStringMapFromJson() {
    Map<String, String> expected = new HashMap<>();
    expected.put("a", "b");
    expected.put("c", "d");
    String json = JsonMapping.Write.mapAsJson(expected).map(JSONObject::toString).getOrElse("");
    Map<String, String> actual = JsonMapping.Read.stringMapFromJson(json).getOrNull();
    Assert.assertArrayEquals(expected.keySet().toArray(), actual.keySet().toArray());
    Assert.assertArrayEquals(expected.values().toArray(), actual.values().toArray());
  }

  @Test
  public void testWriteAndReadJsonString() {
    Map<String, String> expected = new HashMap<>();
    expected.put("test", "test");
    String json = "{'test': 'test'}";
    Map<String, String> actual = JsonMapping.Read.stringMapFromJson(json).getOrNull();
    Assert.assertArrayEquals(expected.keySet().toArray(), actual.keySet().toArray());
    Assert.assertArrayEquals(expected.values().toArray(), actual.values().toArray());
  }

  @Test
  public void testWriteAndReadCorruptJsonStringMultipleBrackets() {
    Map<String, String> expected = new HashMap<>();
    expected.put("test", "test");
    String json = "{'test': 'test'}}";
    Map<String, String> actual = JsonMapping.Read.stringMapFromJson(json).getOrNull();
    Assert.assertArrayEquals(expected.keySet().toArray(), actual.keySet().toArray());
    Assert.assertArrayEquals(expected.values().toArray(), actual.values().toArray());
  }

  @Test
  public void testWriteAndReadCorruptJsonString2() {
    Map<String, String> expected = new HashMap<>();
    expected.put("test", "test");
    String json = "{'test': 'test'}}";
    Map<String, String> actual = JsonMapping.Read.stringMapFromJson(json).getOrNull();
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
                        JsonMapping.Read.mapFromJson(json).getOrNull())
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

    Map<String, Object> actual = JsonMapping.Read.objectMapFromJson(json).getOrNull();
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
    String json = JsonMapping.Write.objectAsJson(expected).map(JSONObject::toString).getOrElse("");
    ApiTokenObjectStub maybeActual =
        JsonMapping.Read.valueFromJson(json, ApiTokenObjectStub.class).getOrNull();
    Assert.assertEquals(expected, maybeActual);
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
    JSONObject json = JsonMapping.Write.objectAsJson(expected).getOrNull();
    ApiTokenObjectStub actual = new ApiTokenObjectStub();
    JsonMapping.Read.populateInstanceFromJson(json, actual);
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
    JSONObject json = JsonMapping.Write.objectAsJson(user).getOrNull();
    /* json:
        {
            "ID": 8777,
            "username": "jondoe",
            "email": "jondoe@example.com",
            "groups": ["group 1", "group 2"]
        }
    */
    User actual = JsonMapping.Read.valueFromJson(json, User.class).getOrNull();
    Assert.assertEquals(user, actual); // true
  }
}
