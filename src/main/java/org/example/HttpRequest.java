package org.example;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

  private Map<String, String> headers;
  private final Map<String, String> queryStringMap;
  private String httpMethod;
  private String requestPath;
  private String httpVersion;
  private String clientAddress;
  private String requestPayload;


  public HttpRequest() {
    headers = new HashMap<>();
    queryStringMap = new HashMap<>();
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }

  public String getHttpVersion() {
    return httpVersion;
  }

  public void setHttpVersion(String httpVersion) {
    this.httpVersion = httpVersion;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public String getRequestPath() {
    return requestPath;
  }

  public void setRequestPath(String requestPath) {
    this.requestPath = requestPath;
  }

  public String getClientAddress() {
    return clientAddress;
  }

  public void setClientAddress(String clientAddress) {
    this.clientAddress = clientAddress;
  }

  public String getRequestPayload() {
    return requestPayload;
  }

  public void setRequestPayload(String requestPayload) {
    this.requestPayload = requestPayload;
  }

  public Map<String, String> getAllQueryString() {
    return queryStringMap;
  }

  public String getQueryString(String query) {
    return queryStringMap.get(query);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder(httpMethod);
    sb.append(" ")
    .append(requestPath).append(" ").append(httpVersion).append("\n");
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
    }

    for (Map.Entry<String, String> entry : queryStringMap.entrySet()) {
      sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
    }
    sb.append(requestPayload);

    return sb.toString();
  }
}
