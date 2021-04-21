package com.kantegasso.jsonmapping.error;

public class ErrorMessage {
  public static String createMessageWithCode(String code, String message) {
    return String.format("[%s] %s", code, message);
  }
}
