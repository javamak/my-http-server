package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Class for HTTP request parsing as defined by RFC 2612:
 * Request = Request-Line ; Section 5.1 (( general-header ; Section 4.5 |
 * request-header ; Section 5.3 | entity-header ) CRLF) ; Section 7.1 CRLF [
 * message-body ] ; Section 4.3
 *
 * @author izelaya
 *
 */
public class HttpRequestParser {

    private String _requestLine;
    private final Hashtable<String, String> _requestHeaders;
    private final StringBuffer _messagetBody;
    private String httpMethod;
    private String requestPath;
    private String httpVersion;

    public HttpRequestParser(BufferedReader reader) throws HttpFormatException, IOException {
        _requestHeaders = new Hashtable<String, String>();
        _messagetBody = new StringBuffer();
        parseRequest(reader);
    }

    /**
     * Parse and HTTP request.
     *
     * @param reader
     *            String holding http request.
     * @throws IOException
     *             If an I/O error occurs reading the input stream.
     * @throws HttpFormatException
     *             If HTTP Request is malformed
     */
    private void parseRequest(BufferedReader reader) throws IOException, HttpFormatException {

        var reqLine = reader.readLine();
        setRequestLine(reqLine); // Request-Line ; Section 5.1
        var arr = reqLine.split(" ");
        httpMethod = arr[0].trim();
        requestPath = arr[1].trim();
        httpVersion = arr[2].trim();


        String header = reader.readLine();
        while (!header.isBlank()) {
            appendHeaderParameter(header);
            header = reader.readLine();
        }

//        String bodyLine = reader.readLine();
//        while (!bodyLine.isBlank()) {
//            appendMessageBody(bodyLine);
//            bodyLine = reader.readLine();
//        }
    }

    /**
     *
     * 5.1 Request-Line The Request-Line begins with a method token, followed by
     * the Request-URI and the protocol version, and ending with CRLF. The
     * elements are separated by SP characters. No CR or LF is allowed except in
     * the final CRLF sequence.
     *
     * @return String with Request-Line
     */
    public String getRequestLine() {
        return _requestLine;
    }

    private void setRequestLine(String requestLine) throws HttpFormatException {
        if (requestLine == null || requestLine.isEmpty()) {
            throw new HttpFormatException("Invalid Request-Line: " + requestLine);
        }
        _requestLine = requestLine;
    }

    private void appendHeaderParameter(String header) throws HttpFormatException {
        int idx = header.indexOf(":");
        if (idx == -1) {
            throw new HttpFormatException("Invalid Header Parameter: " + header);
        }
        _requestHeaders.put(header.substring(0, idx), header.substring(idx + 1));
    }

    /**
     * The message-body (if any) of an HTTP message is used to carry the
     * entity-body associated with the request or response. The message-body
     * differs from the entity-body only when a transfer-coding has been
     * applied, as indicated by the Transfer-Encoding header field (section
     * 14.41).
     * @return String with message-body
     */
    public String getMessageBody() {
        return _messagetBody.toString();
    }

    private void appendMessageBody(String bodyLine) {
        _messagetBody.append(bodyLine).append("\r\n");
    }

    /**
     * For list of available headers refer to sections: 4.5, 5.3, 7.1 of RFC 2616
     * @param headerName Name of header
     * @return String with the value of the header or null if not found.
     */
    public String getHeaderParam(String headerName){
        return _requestHeaders.get(headerName);
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public String getHttpVersion() {
        return httpVersion;
    }
}