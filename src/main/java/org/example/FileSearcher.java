
package org.example;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FileSearcher implements Runnable {
    private final String searchTerm;
    private final String filePath;
    private final long start;
    private final long end;
    private final AtomicInteger matchCount;
    private final AtomicLong totalTime;
    private final CountDownLatch latch;
    private final Pattern pattern;

    public FileSearcher(String searchTerm, String filePath, long start, long end, AtomicInteger matchCount, AtomicLong totalTime, CountDownLatch latch) {
        if (searchTerm == null || filePath == null) {
            throw new IllegalArgumentException("Search term and file path must not be null.");
        }
        this.searchTerm = searchTerm;
        this.filePath = filePath;
        this.start = start;
        this.end = end;
        this.matchCount = matchCount;
        this.totalTime = totalTime;
        this.latch = latch;

        // Compile pattern once per thread
    /*    String escapedMessage = Pattern.quote(searchTerm);*/
        this.pattern = Pattern.compile("\"message\":\"(.*?" + searchTerm + ".*?)\"");
    }

    @Override
    public void run() {
        long threadStartTime = System.currentTimeMillis();

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r");
             BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(raf.getFD())))) {

            raf.seek(start);

            String line;
            while (raf.getFilePointer() < end && (line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    synchronized (System.out) {
                        System.out.println("Match found for \"" + searchTerm + "\": " + line);
                    }
                    matchCount.incrementAndGet();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        long threadEndTime = System.currentTimeMillis();
        totalTime.addAndGet(threadEndTime - threadStartTime);
        latch.countDown(); // Notify that this thread has finished
    }
}

