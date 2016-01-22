package com.happysathya.sample;

import java.io.IOException;
import java.net.URI;

public class FileWatcherRunner {
    public static void main(String[] args) {
        try (FileWatcher fileWatcher = new FileWatcher(URI.create(args[0]))) {
            fileWatcher.startWatch();
            System.out.println("press enter to exit!!!");
            System.in.read();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
