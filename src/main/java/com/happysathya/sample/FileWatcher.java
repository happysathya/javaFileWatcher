package com.happysathya.sample;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher implements Closeable {

    private final ScheduledExecutorService scheduledExecutorService;
    private final WatchService watchService;
    private final URI uri;

    private volatile WatchKey watchKey;
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
        Optional.ofNullable(watchKey).ifPresent(WatchKey::cancel);
    }

    private void processEvents() {
        if (!hasRegisteredSuccessfully) {
            try {
                watchKey = Paths.get(uri).register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                hasRegisteredSuccessfully = true;
            } catch (IOException e) {
                return;
            }
        }
        if (!watchKey.isValid()) {
            hasRegisteredSuccessfully = false;
            return;
        }
        watchKey.pollEvents().stream().forEach(watchEvent -> {
            if (watchEvent.kind() != OVERFLOW) {
                WatchEvent<Path> boundedWatchEvent = (WatchEvent<Path>) watchEvent;
                Path pathChanged = boundedWatchEvent.context();
                System.out.println("**************************************");
                System.out.println("Eventname: " + boundedWatchEvent.kind().name());
                System.out.println("File changed: " + pathChanged.toString());
                System.out.println("**************************************");
            }
        });
        if (!watchKey.reset()) {
            hasRegisteredSuccessfully = false;
            return;
        }
    }
}
