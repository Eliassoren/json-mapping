package com.kantegasso.jsonmapping.error;

public class JsonMappingException extends RuntimeException {
  public final String errorCode;

  public JsonMappingException(String code, String message) {
    super(ErrorMessage.createMessageWithCode(code, message));
    errorCode = code;
  }

  public JsonMappingException(String code, String message, Throwable cause) {
    super(ErrorMessage.createMessageWithCode(code, message), cause);
    errorCode = code;
  }
}
