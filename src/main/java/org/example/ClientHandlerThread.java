package org.example;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
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

  private void generateHttpResponse(HttpRequest request, HttpResponse response) {

    byte[] responseContent = new byte[0];
    HttpStatus httpStatus = HttpStatus.OK;
    try {
      responseContent = pageCache.readFromFileOrCache(request.getRequestPath());
    } catch (IOException e) {
      httpStatus = HttpStatus.NOT_FOUND;
    }
    var acceptedEncodings = request.getHeaders().get("accept-encoding");
    if (responseContent.length > 0
        && acceptedEncodings != null
        && acceptedEncodings.contains("gzip")) {

      response.getHeaders().put("Content-Encoding", "gzip");
      response.getHeaders().put("Vary", "Accept-Encoding");
      var byteArrayOutputStream = new ByteArrayOutputStream();
      GZIPOutputStream gzip = null;
      try {
        gzip = new GZIPOutputStream(byteArrayOutputStream);
        gzip.write(responseContent);
        gzip.finish();
        gzip.close();
        responseContent = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
      } catch (IOException e) {
        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        responseContent = HttpStatus.INTERNAL_SERVER_ERROR.getDescription().getBytes();
      }
    }

    response.setHttpStatus(httpStatus);
    response.setResponse(responseContent);
    response.getHeaders().put("Content-Length", "" + responseContent.length);
  }

  private void writeResponseToSocket(OutputStream bw, HttpResponse httpResponse) {
    if (bw != null) {
      try {
        bw.write("HTTP/1.1 ".getBytes());
        bw.write((httpResponse.getHttpStatus().toString()).getBytes());
        bw.write(NEWLINE_BYTES);
        for (Map.Entry<String, String> entry : httpResponse.getHeaders().entrySet()) {
          bw.write((entry.getKey() + ": " + entry.getValue()).getBytes());
          bw.write(NEWLINE_BYTES);
        }
        bw.write(httpResponse.getContentType().getBytes());
        bw.write(NEWLINE_BYTES);
        bw.write(NEWLINE_BYTES);

        bw.write(httpResponse.getResponse());
        bw.write(NEWLINE_BYTES);

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
    try {

      bw = new BufferedOutputStream(socket.getOutputStream());

      var httpRequest = HttpRequestParser.getInstance().parseRequest(socket.getInputStream());
      generateHttpResponse(httpRequest, httpResponse);
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
      writeResponseToSocket(bw, httpResponse);
    }
  }
}
