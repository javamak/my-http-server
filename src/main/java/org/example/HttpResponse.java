package org.example;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

  private Map<String, String> headers;
  private byte[] response;
  private HttpStatus httpStatus;
  private String contentType;
  public HttpResponse() {
    headers = new HashMap<>();
    contentType = "Content-Type: text/html; charset=UTF-8";
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public byte[] getResponse() {
    return response;
  }

  public void setResponse(byte[] response) {
    this.response = response;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public void setHttpStatus(HttpStatus httpStatus) {
    this.httpStatus = httpStatus;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
}
