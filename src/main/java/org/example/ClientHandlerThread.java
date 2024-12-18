package org.example;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class ClientHandlerThread extends Thread {
  public static final byte[] NEWLINE_BYTES = "\r\n".getBytes();
  Socket socket;
  PageCache pageCache;

  public ClientHandlerThread(Socket socket, PageCache cache) throws SocketException {
    socket.setTcpNoDelay(false);
    socket.setSendBufferSize(64000);
    socket.setReceiveBufferSize(64000);
    this.socket = socket;
    this.pageCache = cache;
  }

  private void generateHttpResponse(HttpRequest request, HttpResponse response) throws IOException {

    byte[] responseContent = pageCache.readFromFileOrCache(request.getRequestPath());

    response.setHttpStatus(HttpStatus.OK);
    response.setResponse(responseContent);
  }

  void validateResponseAndCompress(HttpRequest httpRequest, HttpResponse httpResponse) {
    var acceptedEncodings = httpRequest.getHeaders().get("accept-encoding");

    if (httpResponse.getResponse() == null
            || httpResponse.getResponse().length == 0) {
      // Setting the body as error description.
      if (!(httpResponse.getHttpStatus().getCode() >= 200
              && httpResponse.getHttpStatus().getCode() < 300)) {
        httpResponse.setResponse(
                httpResponse.getHttpStatus().getDescription().getBytes(StandardCharsets.UTF_8));
      }
    }

    if (acceptedEncodings != null
        && acceptedEncodings.contains("gzip")) {

      httpResponse.getHeaders().put("Content-Encoding", "gzip");
      httpResponse.getHeaders().put("Vary", "Accept-Encoding");
      var byteArrayOutputStream = new ByteArrayOutputStream();
      GZIPOutputStream gzip = null;
      try {
        gzip = new GZIPOutputStream(byteArrayOutputStream);
        gzip.write(httpResponse.getResponse());
        gzip.finish();
        gzip.close();
        httpResponse.setResponse(byteArrayOutputStream.toByteArray());
        byteArrayOutputStream.close();
      } catch (IOException e) {
        httpResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        httpResponse.setResponse(HttpStatus.INTERNAL_SERVER_ERROR.getDescription().getBytes());
      }
    }
  }

  private void writeResponseToSocket(
      OutputStream bw, HttpResponse httpResponse, HttpRequest httpRequest) {
    if (bw != null) {
      try {

        validateResponseAndCompress(httpRequest, httpResponse);

        bw.write("HTTP/1.1 ".getBytes());
        bw.write((httpResponse.getHttpStatus().toString()).getBytes());
        bw.write(NEWLINE_BYTES);
        for (Map.Entry<String, String> entry : httpResponse.getHeaders().entrySet()) {
          bw.write((entry.getKey() + ": " + entry.getValue()).getBytes());
          bw.write(NEWLINE_BYTES);
        }
        bw.write(httpResponse.getContentType().getBytes());
        bw.write(NEWLINE_BYTES);
        bw.write(("Content-Length: " + httpResponse.getResponse().length).getBytes());
        bw.write(NEWLINE_BYTES);
        bw.write(NEWLINE_BYTES);

        bw.write(httpResponse.getResponse());

        bw.flush();
        bw.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    try {
      socket.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void run() {
    OutputStream bw = null;
    var httpResponse = new HttpResponse();
    var httpRequest = new HttpRequest();
    try {

      bw = new BufferedOutputStream(socket.getOutputStream());

      HttpRequestParser.getInstance().parseRequest(socket.getInputStream(), httpRequest);
      System.out.println(httpRequest);
      generateHttpResponse(httpRequest, httpResponse);
    } catch (SocketTimeoutException e) {
      e.printStackTrace();
      httpResponse.setHttpStatus(HttpStatus.REQUEST_TIMEOUT);
    } catch (IOException e) {
      e.printStackTrace();
      httpResponse.setHttpStatus(HttpStatus.NOT_FOUND);
    } catch (HttpFormatException e) {
      e.printStackTrace();
      httpResponse.setHttpStatus(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      e.printStackTrace();
      httpResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    } finally {
      writeResponseToSocket(bw, httpResponse, httpRequest);
    }
  }
}
