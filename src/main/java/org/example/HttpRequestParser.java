package org.example;

import java.io.BufferedReader;
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

    parseRequestLine(reader.readLine(), httpRequest);

    String header = reader.readLine();

    while (!header.isBlank()) {
      appendHeaderParameter(header, httpRequest);
      header = reader.readLine();
    }

    readBody(reader, httpRequest);

    return httpRequest;
  }

  private void parseRequestLine(String reqLine, HttpRequest httpRequest) {
    var hasQuery = reqLine.indexOf("?");
    if (hasQuery > -1) {

      var firstSpace = reqLine.indexOf(' ');
      var lastSpace = reqLine.lastIndexOf(' ');

      httpRequest.setHttpMethod(reqLine.substring(0, firstSpace).trim());
      httpRequest.setHttpVersion(reqLine.substring(lastSpace).trim());

      var reqPathWithQuery = reqLine.substring(firstSpace, lastSpace).trim();

      var arr = reqPathWithQuery.split("\\?");
      httpRequest.setRequestPath(arr[0].trim());
      var queryString = arr[1];
      arr = queryString.split("&");
      for (String s : arr) {
        var queryArr = s.split("=");
        httpRequest.getAllQueryString().put(queryArr[0].trim(), queryArr[1].trim());
      }
    } else {
      var arr = reqLine.split(" ");
      httpRequest.setHttpMethod(arr[0].trim());
      httpRequest.setRequestPath(arr[1].trim());
      httpRequest.setHttpVersion(arr[2].trim());
    }
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
    httpRequest
        .getHeaders()
        .put(header.substring(0, idx).toLowerCase(), header.substring(idx + 1).trim());
  }
}
