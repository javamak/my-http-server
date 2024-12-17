package org.example;

import org.junit.Test;

public class Parser {

  @Test
  public void test() {
    var input = "POST /index.html?name=arun&user=kumar HTTP/1.1";
    var request = new HttpRequest();
    parseRequestLine(input, request);

    System.out.println(request);
  }

  private void parseRequestLine(String reqLine, HttpRequest httpRequest) {
    var hasQuery = reqLine.indexOf("?");
    if (hasQuery > -1) {

      var firstSpace = reqLine.indexOf(' ');
      var lastSpace = reqLine.lastIndexOf(' ');

      httpRequest.setHttpMethod(reqLine.substring(0, firstSpace));
      httpRequest.setHttpVersion(reqLine.substring(lastSpace));

      var reqPathWithQuery = reqLine.substring(firstSpace, lastSpace);

      var arr = reqPathWithQuery.split("\\?");
      httpRequest.setRequestPath(arr[0]);
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
}
