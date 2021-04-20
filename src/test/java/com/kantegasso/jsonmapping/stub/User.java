package com.kantegasso.jsonmapping.stub;

import com.kantegasso.jsonmapping.JsonMapping.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {
  @JsonProperty("ID")
  private final int ID;

  @JsonProperty("username")
  private String username;

  @JsonProperty("email")
  private String email;

  @JsonProperty("groups")
  private List<String> groups;

  @JsonProperty("contacts")
  List<User> contacts = new ArrayList<>();

  User() {
    this.ID = -1;
  }

  User(int ID) {
    this.ID = ID;
  }

  public int getID() {
    return ID;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public List<String> getGroups() {
    return groups;
  }

  public void setGroups(List<String> groups) {
    this.groups = groups;
  }

  public List<User> getContacts() {
    return contacts;
  };

  public void setContacts(List<User> contacts) {
    this.contacts = contacts;
  };

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return ID == user.ID
        && Objects.equals(username, user.username)
        && Objects.equals(email, user.email)
        && Objects.equals(groups, user.groups);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ID, username, email, groups);
  }
}
