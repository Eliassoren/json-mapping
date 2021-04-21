package com.kantegasso.jsonmapping.penetrationtest;

public class InsecureObject {
  private Object name;
  private int age;

  public InsecureObject() {}

  public Object getName() {
    return name;
  }

  public void setName(Object name) {
    this.name = name;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }
}
