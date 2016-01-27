package com.happysathya.sample;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher implements Closeable {

    private final ScheduledExecutorService scheduledExecutorService;
    private final WatchService watchService;
    private final URI uri;

    private volatile boolean hasRegisteredSuccessfully = false;

    public FileWatcher(URI uri) throws IOException {
        this.uri = uri;
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        watchService = FileSystems.getDefault().newWatchService();
    }

    public void startWatch() {
        scheduledExecutorService.scheduleWithFixedDelay(this::processEvents, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void close() throws IOException {
        scheduledExecutorService.shutdown();
        watchService.close();
    }

    private void processEvents() {
        if (!hasRegisteredSuccessfully) {
            try {
                Files.walkFileTree(Paths.get(uri), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                        return FileVisitResult.CONTINUE;
                    }
                });
                hasRegisteredSuccessfully = true;
            } catch (IOException e) {
                return;
            }
        }

        while (true) {
            WatchKey watchKey = watchService.poll();
            if (watchKey == null) break;
            Path watchDir = (Path) watchKey.watchable();
            watchKey.pollEvents().stream().forEach(watchEvent -> {
                WatchEvent<Path> boundedWatchEvent = (WatchEvent<Path>) watchEvent;
                Path pathChanged = boundedWatchEvent.context();
                if (watchEvent.kind() != OVERFLOW) {
                    System.out.println("**************************************");
                    System.out.println("Eventname: " + boundedWatchEvent.kind().name());
                    System.out.println("File changed: " + pathChanged.toString());
                    System.out.println("**************************************");
                }
                if (watchEvent.kind() == ENTRY_CREATE) {
                    try {
                        Path newDirectoryToWatch = watchDir.resolve(pathChanged);
                        if (Files.isDirectory(newDirectoryToWatch)) {
                            newDirectoryToWatch.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            watchKey.reset();
        }
    }
}
