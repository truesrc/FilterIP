package me.truesrc.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.net.InetAddresses.isInetAddress;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;

@Service
public class IpBlocksHelper extends Thread {
    private static Logger log = LoggerFactory.getLogger(IpBlocksHelper.class);
    @Value("${absolute.path.directory.blacklist}")
    private String dirBlacklistName;
    @Value("${blacklist.name}")
    private String blacklistFileName;

    Set<String> getIps() {
        return ips;
    }

    private void setIps(Set<String> ips) {
        this.ips = ips;
    }

    private Set<String> ips = ConcurrentHashMap.newKeySet();


    private void readBlacklistFile() {
        try (BufferedReader in =
                     Files.newBufferedReader(Paths.get(dirBlacklistName + blacklistFileName))) {
            setIps(in.lines().flatMap(str ->
                    stream(str.split(",")).filter(ip -> {
                        if (!isInetAddress(ip)) {
                            log.error(ip);
                            return false;
                        } else return true;
                    })
            ).collect(Collectors.toSet()));
            log.info("blacklist ips: " + ips);
        } catch (IOException | IllegalArgumentException e) {
            log.error(e.getMessage());
        }

    }

    private void watchForBlacklistFile() {
        Path path = Paths.get(dirBlacklistName);
        WatchService watchService = null;
        try {
            watchService = path.getFileSystem().newWatchService();
            path.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        // Бесконечный цикл
        for (; ; ) {
            WatchKey key = null;
            try {
                key = watchService.take();
                sleep(500); // give a chance for duplicate events to pile up
            } catch (InterruptedException | NullPointerException e) {
                log.error(e.getMessage());
            }

            // Итерации для каждого события
            for (WatchEvent event : key.pollEvents()) {
                if (event.context().toString().equals(blacklistFileName)) {
                    log.info("blacklist.txt CREATE or MODIFY");
                    readBlacklistFile();
                    break;
                }
            }
            // Сброс ключа важен для получения последующих уведомлений
            key.reset();
        }
    }

    @Override
    public void run() {
        readBlacklistFile();
        watchForBlacklistFile();
    }

    static String getClientIpAddress(ServletRequest request) {
        String xForwardedForHeader = ((HttpServletRequest) request).getHeader("X-Forwarded-For");
        if (isNull(xForwardedForHeader)) {
            return request.getRemoteAddr();
        } else {
            // As of https://en.wikipedia.org/wiki/X-Forwarded-For
            // The general format of the field is: X-Forwarded-For: client, proxy1, proxy2 ...
            // we only want the client
            return new StringTokenizer(xForwardedForHeader, ",").nextToken().trim();
        }
    }
}
