package com.happysathya.sample;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class FileWatcherTest {

    @Test
    public void test_should_wait_for_file_events() throws IOException, InterruptedException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(byteArrayOutputStream);
        System.setOut(ps);

        Path tempPath = Files.createTempDirectory("temp42");
        FileWatcher fileWatcher = new FileWatcher(tempPath.toUri());
        fileWatcher.startWatch();

        //executor service has an initial delay. so we will wait to catchup
        Thread.sleep(1000 * 10);

        Path tempFile1 = Files.createTempFile(tempPath, "temp1", "txt");
        Path tempFile2 = Files.createTempFile(tempPath, "temp2", "txt");
        //wait for initial events to catchup
        Thread.sleep(1000 * 10);

        ps.flush();
        String dataToBeInspected = byteArrayOutputStream.toString();
        assertTrue(dataToBeInspected.contains(tempFile1.getFileName().toString()));
        assertTrue(dataToBeInspected.contains(tempFile2.getFileName().toString()));

        fileWatcher.close();

        Files.delete(tempFile1);
        Files.delete(tempFile2);
        Files.delete(tempPath);
    }

}