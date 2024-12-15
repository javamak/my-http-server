package org.example;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class PageCache {

    HashMap<String, byte[]> cache = new HashMap<>();
    ReadWriteLock rwLock = new ReentrantReadWriteLock();
    Lock readLock = rwLock.readLock();
    Lock writeLock = rwLock.writeLock();

    private static final PageCache INSTANCE = new PageCache();

    private PageCache() {
    }

    public static PageCache getINSTANCE() {
        return INSTANCE;
    }

    public byte[] readFromFileOrCache(String requestPath) throws IOException {

        try {
            readLock.lock();
            if (cache.containsKey(requestPath)) {
                return cache.get(requestPath);
            }
        } finally {
            readLock.unlock();
        }

        try {
            var time = System.currentTimeMillis();
            writeLock.lock();

            var fileStream = new BufferedInputStream(new FileInputStream("/home/arun/Work/my-load-balancer/html" + requestPath));
            byte[] content = fileStream.readAllBytes();
            cache.put(requestPath, content);
            fileStream.close();
            System.out.println("Cache loaded in " + (System.currentTimeMillis() - time) + "ms");
            return content;
        } finally {
            writeLock.unlock();
        }
    }
}
