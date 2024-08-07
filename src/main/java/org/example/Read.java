
package org.example;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Read implements Closeable {
    private static final String PATH = "/Users/srushti/Downloads/onboarding-h.09-01-2024.0 (1).log";
    private final Scanner sc;

    public Read() {
        sc = new Scanner(System.in);
    }

    public void readData() throws IOException {
        // Prompt for the number of threads
        System.out.println("Enter the number of threads to use:");
        int numberOfThreads;
        try {
            numberOfThreads = Integer.parseInt(sc.nextLine());
            if (numberOfThreads <= 0) {
                throw new NumberFormatException("Number of threads must be positive.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number of threads. Using default value of 2.");
            numberOfThreads = 2; // Default value
        }

        // Prompt for the search message
        System.out.println("Enter the message to be searched:");
        String searchMessage = sc.nextLine();

        File file = new File(PATH);
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("The specified file does not exist: " + PATH);
        }

        long fileLength = file.length();
        long chunkSize = fileLength / numberOfThreads;

        AtomicInteger matchCount = new AtomicInteger(0);
        AtomicLong totalTime = new AtomicLong(0);

        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        // Record start time
        long startTime = System.currentTimeMillis();

        // Create and submit tasks
        for (int i = 0; i < numberOfThreads; i++) {
            long start = i * chunkSize;
            long end = (i == numberOfThreads - 1) ? fileLength : (i + 1) * chunkSize;
            executorService.submit(new FileSearcher(searchMessage, PATH, start, end, matchCount, totalTime, latch));
        }

        // Shutdown executor and wait for tasks to complete
        executorService.shutdown();
        try {
            latch.await(); // Wait for all threads to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt status
            e.printStackTrace();
        }

        // Record end time and calculate total time
        long endTime = System.currentTimeMillis();
        long totalTimeTaken = endTime - startTime;

        // Output results
        System.out.println("Total time taken: " + totalTimeTaken + " milliseconds");
        System.out.println("Number of matches found for \"" + searchMessage + "\": " + matchCount.get());
    }

    @Override
    public void close() throws IOException {
        sc.close();
    }
}

