package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {

        AtomicBoolean isRunning = new AtomicBoolean(true);
        Thread shutdownThread = new Thread(() -> {
            isRunning.set(false);
            System.out.println("Shutting down the server");
        });
        Runtime.getRuntime().addShutdownHook(shutdownThread);


        try (var serverSocket = new ServerSocket(9000)) {
//            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            try (var executor = Executors.newFixedThreadPool(10)) {
                System.out.println("Application started. Listening on port 9000");
                var pageCache = PageCache.getINSTANCE();
                while (isRunning.get()) {
                    var socket = serverSocket.accept();
                    executor.submit(new ClientHandlerThread(socket, pageCache));
                }
            }
        }
    }


}
