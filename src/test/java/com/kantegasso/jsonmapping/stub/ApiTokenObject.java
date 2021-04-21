package com.kantegasso.jsonmapping.stub;

import com.kantegasso.jsonmapping.JsonMapping.JsonMapper;
import com.kantegasso.jsonmapping.JsonMapping.JsonProperty;

@JsonMapper
public interface ApiTokenObject {
  @JsonProperty("alias")
  String getAlias();

  @JsonProperty("alias")
  void setAlias(String alias);

  @JsonProperty("validFor")
  long getValidFor();

  @JsonProperty("validFor")
  void setValidFor(long validFor);

  @JsonProperty("createdAt")
  long getCreatedAt();

  @JsonProperty("createdAt")
  void setCreatedAt(long createdAt);

  @JsonProperty("hashed")
  String getHashed();

  @JsonProperty("hashed")
  void setHashed(String hashed);

  @JsonProperty("salt")
  String getSalt();

  @JsonProperty("salt")
  void setSalt(String salt);

  @JsonProperty("userKey")
  String getUserKey();

  @JsonProperty("userKey")
  void setUserKey(String userKey);
}
