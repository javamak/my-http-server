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

    var reader = new BufferedReader(new InputStreamReader((inputStream)));
    var reqLine = reader.readLine();
    var arr = reqLine.split(" ");
    httpRequest.setHttpMethod(arr[0].trim());
    httpRequest.setRequestPath(arr[1].trim());
    httpRequest.setHttpVersion(arr[2].trim());

    String header = reader.readLine();

    while (!header.isBlank()) {
      appendHeaderParameter(header, httpRequest);
      header = reader.readLine();

    }

    readBody(reader, httpRequest);

    return httpRequest;
  }


  private void readBody(BufferedReader reader, HttpRequest request) throws IOException {

    if (request.getHeaders().containsKey("content-length")
        || request.getHeaders().containsKey("content-encoding")
        || request.getHeaders().containsKey("content-type")) {

      var lengthStr = request.getHeaders().get("content-length");

      var length = Integer.parseInt(lengthStr);

      char[] charBuff = new char[length];
      reader.read(charBuff);
      request.setRequestPayload(new String(charBuff));
    }
  }

  private void appendHeaderParameter(String header, HttpRequest httpRequest)
      throws HttpFormatException {
    int idx = header.indexOf(":");
    if (idx == -1) {
      throw new HttpFormatException("Invalid Header Parameter: " + header);
    }
    httpRequest.getHeaders().put(header.substring(0, idx).toLowerCase(), header.substring(idx + 1).trim());
  }
}
