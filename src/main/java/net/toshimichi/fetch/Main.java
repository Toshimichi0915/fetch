package net.toshimichi.fetch;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends PlaceholderExpansion {

    private static final Pattern PATTERN = Pattern.compile("^ *?([^ ]+?) +?([^ ]+?) +?([^ ]+?) *?$");
    private static final Path cachePath = Path.of("./fetch");

    private final Map<String, CacheData> primaryCache = new HashMap<>();

    private boolean isFileExpired(LocalDateTime lastModified, int expire) {
        if (expire < 0) return false;
        return Duration.between(lastModified, LocalDateTime.now()).toSeconds() * 20 > expire;
    }

    private LocalDateTime getLastModified(Path path) throws IOException {
        FileTime lastModifiedTime = Files.getLastModifiedTime(path);
        return LocalDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault());
    }

    private String primaryCache(String name, int expire) throws IOException {
        CacheData cacheData = primaryCache.get(name);
        if (cacheData == null) return null;
        if (isFileExpired(cacheData.getLastModified(), expire)) return null;
        return cacheData.getContents();
    }

    private String secondaryCache(String name, int expire, URL url) throws IOException {
        Path path = cachePath.resolve(name);
        if (!Files.exists(path) || isFileExpired(getLastModified(path), expire)) {
            Files.createDirectories(cachePath);
            try (InputStream in = url.openStream()) {
                Files.copy(in, path);
            }
        }

        String contents = Files.readString(path);

        // insert data into primary cache
        primaryCache.put(name, new CacheData(contents, getLastModified(path)));

        return contents;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        Matcher matcher = PATTERN.matcher(params);
        if (!matcher.find()) return null;
        try {
            String name = matcher.group(1);
            int expire = Integer.parseInt(matcher.group(2));
            URL url = new URL(matcher.group(3));

            String contents = primaryCache(name, expire);
            if (contents != null) return contents;
            return secondaryCache(name, expire, url);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    @Override
    public String getIdentifier() {
        return "fetch";
    }

    @Override
    public String getAuthor() {
        return "Toshimichi";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
}
