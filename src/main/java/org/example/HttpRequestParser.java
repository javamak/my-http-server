package org.example;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpRequestParser {

  private static final HttpRequestParser _INSTANCE = new HttpRequestParser();

  private HttpRequestParser() {}

  public static HttpRequestParser getInstance() {
    return _INSTANCE;
  }

  public HttpRequest parseRequest(InputStream inputStream) throws IOException, HttpFormatException {

    var httpRequest = new HttpRequest();

    var available = inputStream.available();
    BufferedReader reader = null;
    if (available == 0) {
      reader = new BufferedReader(new InputStreamReader((inputStream)));
    } else {
      var byteArr = new byte[available];
      inputStream.read(byteArr);
      reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(byteArr)));
    }

    parseRequest(reader, httpRequest);

    return httpRequest;
  }

  private void parseRequest(BufferedReader reader, HttpRequest httpRequest)
      throws IOException, HttpFormatException {
    var reqLine = reader.readLine();
    var arr = reqLine.split(" ");
    httpRequest.setHttpMethod(arr[0].trim());
    httpRequest.setRequestPath(arr[1].trim());
    httpRequest.setHttpVersion(arr[2].trim());
    System.out.println(reqLine);

    String header = reader.readLine();
    System.out.println(header);
    while (!header.isBlank()) {
      appendHeaderParameter(header, httpRequest);
      header = reader.readLine();
      System.out.println(header);
    }

    readBody(reader, httpRequest);
    System.out.println(httpRequest.getRequestPayload());
  }

  private void readBody(BufferedReader reader, HttpRequest request) throws IOException {

    if (request.getHeaders().containsKey("content-length")
        || request.getHeaders().containsKey("content-encoding")
        || request.getHeaders().containsKey("content-type")) {
      String bodyLine = reader.readLine();
      var sb = new StringBuilder();
      while (bodyLine != null && !bodyLine.isBlank()) {
        sb.append(bodyLine);
        bodyLine = reader.readLine();
      }
      request.setRequestPayload(sb.toString());
    }
  }

  private void appendHeaderParameter(String header, HttpRequest httpRequest)
      throws HttpFormatException {
    int idx = header.indexOf(":");
    if (idx == -1) {
      throw new HttpFormatException("Invalid Header Parameter: " + header);
    }
    httpRequest.getHeaders().put(header.substring(0, idx).toLowerCase(), header.substring(idx + 1));
  }
}
