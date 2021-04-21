package com.kantegasso.jsonmapping.stub;

import java.beans.PropertyChangeListener;
import java.util.Objects;

public class ApiTokenObjectStubWithoutAnnotation implements ApiTokenObject {
  private int ID;
  private String alias;
  private long createdAt;
  private String hashed;
  private String salt;
  private String userKey;
  private long validFor;

  public ApiTokenObjectStubWithoutAnnotation() {
    this.ID = 3;
  }

  @Override
  public String getAlias() {
    return alias;
  }

  @Override
  public void setAlias(String alias) {
    this.alias = alias;
  }

  @Override
  public long getValidFor() {
    return validFor;
  }

  @Override
  public void setValidFor(long validFor) {
    this.validFor = validFor;
  }

  @Override
  public long getCreatedAt() {
    return createdAt;
  }

  @Override
  public void setCreatedAt(long createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public String getHashed() {
    return hashed;
  }

  @Override
  public void setHashed(String hashed) {
    this.hashed = hashed;
  }

  @Override
  public String getSalt() {
    return salt;
  }

  @Override
  public void setSalt(String salt) {
    this.salt = salt;
  }

  @Override
  public String getUserKey() {
    return userKey;
  }

  @Override
  public void setUserKey(String userKey) {
    this.userKey = userKey;
  }

  public int getID() {
    return ID;
  }

  public void init() {}

  public void save() {}

  public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {}

  public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {}

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiTokenObjectStubWithoutAnnotation that = (ApiTokenObjectStubWithoutAnnotation) o;
    return ID == that.ID
        && createdAt == that.createdAt
        && validFor == that.validFor
        && Objects.equals(alias, that.alias)
        && Objects.equals(hashed, that.hashed)
        && Objects.equals(salt, that.salt)
        && Objects.equals(userKey, that.userKey);
  }

  public int hashCode() {
    return Objects.hash(ID, alias, createdAt, hashed, salt, userKey, validFor);
  }
};
