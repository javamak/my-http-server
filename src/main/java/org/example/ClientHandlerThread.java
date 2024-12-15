package org.example;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

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


    @Override
    public void run() {
        OutputStream bw = null;
        try {

            var br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bw = new BufferedOutputStream(socket.getOutputStream());
            var reqParser = new HttpRequestParser(br);

            bw.write("HTTP/1.1 200 OK\r\n".getBytes());
            bw.write(NEWLINE_BYTES);
            bw.write(pageCache.readFromFileOrCache(reqParser.getRequestPath()));
            bw.write(NEWLINE_BYTES);
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (bw != null) {
                try {
                    bw.write("HTTP/1.1 404 NotFound\r\n".getBytes());
                    bw.write(NEWLINE_BYTES);
                    bw.write("File Not found\r\n".getBytes());

                    bw.flush();
                    bw.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } catch (HttpFormatException e) {
            e.printStackTrace();
            if (bw != null) {
                try {
                    bw.write("HTTP/1.1 400 BadRequest\r\n".getBytes());
                    bw.write(NEWLINE_BYTES);
                    bw.write((e.getMessage() + "\r\n").getBytes());

                    bw.flush();
                    bw.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            } else {
                throw new RuntimeException("Outputstream is null");
            }

        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
