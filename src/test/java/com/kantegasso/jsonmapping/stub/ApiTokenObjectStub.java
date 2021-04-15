package com.kantegasso.jsonmapping.stub;

import java.beans.PropertyChangeListener;
import java.util.Objects;

public class ApiTokenObjectStub {
  private int ID;
  private String alias;
  private long createdAt;
  private String hashed;
  private String salt;
  private String userKey;
  private long validFor;

  public ApiTokenObjectStub() {
    this.ID = 3;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public long getValidFor() {
    return validFor;
  }

  public void setValidFor(long validFor) {
    this.validFor = validFor;
  }

  public long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(long createdAt) {
    this.createdAt = createdAt;
  }

  public String getHashed() {
    return hashed;
  }

  public void setHashed(String hashed) {
    this.hashed = hashed;
  }

  public String getSalt() {
    return salt;
  }

  public void setSalt(String salt) {
    this.salt = salt;
  }

  public String getUserKey() {
    return userKey;
  }

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
    ApiTokenObjectStub that = (ApiTokenObjectStub) o;
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