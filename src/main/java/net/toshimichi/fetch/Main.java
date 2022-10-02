package net.toshimichi.fetch;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

    private static final Pattern PARAM_PATTERN = Pattern.compile("^ *?([^ ]+?) +?([^ ]+?) +?([^ ]+?) *?$");
    private static final Pattern FILE_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    private static final Path cachePath = Path.of("./fetch");

    private final Map<String, CacheData> primaryCache = new HashMap<>();

    private boolean isCacheExpired(LocalDateTime dateTime, int expire) {
        if (expire < 0) return false;
        return Duration.between(dateTime, LocalDateTime.now()).toSeconds() * 20 > expire;
    }

    private LocalDateTime getLastModified(Path path) throws IOException {
        FileTime lastModifiedTime = Files.getLastModifiedTime(path);
        return LocalDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault());
    }

    private String primaryCache(String name, int expire) {
        CacheData cacheData = primaryCache.get(name);
        if (cacheData == null) return null;
        if (isCacheExpired(cacheData.getLastModified(), expire)) return null;
        return cacheData.getContents();
    }

    private String secondaryCache(String name, int expire, Path path) throws IOException {
        if (!Files.exists(path)) return null;
        if (isCacheExpired(getLastModified(path), expire)) return null;

        String contents = Files.readString(path);

        // insert data into primary cache
        primaryCache.put(name, new CacheData(contents, getLastModified(path)));

        return contents;
    }

    private String fetch(String name, Path path, URL url) throws IOException {
        String contents;
        try (InputStream in = url.openStream()) {
            Files.createDirectories(cachePath);
            contents = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        // insert data into primary cache
        primaryCache.put(name, new CacheData(contents, LocalDateTime.now()));

        // insert data into secondary cache
        Files.writeString(path, contents);

        return contents;
    }

    @Override
    public String onRequest(OfflinePlayer player, String param) {
        Matcher matcher = PARAM_PATTERN.matcher(param);
        if (!matcher.find()) return null;
        try {
            String name = matcher.group(1);
            int expire = Integer.parseInt(matcher.group(2));
            URL url = new URL(matcher.group(3));

            // validation
            String protocol = url.getProtocol();
            if (!protocol.equals("http") && !protocol.equals("https")) {
                return "INVALID_PROTOCOL";
            }

            if (!FILE_PATTERN.matcher(name).matches()) {
                return "INVALID_NAME";
            }

            // cache
            String contents = primaryCache(name, expire);
            if (contents != null) return contents;

            Path path = cachePath.resolve(name);
            contents = secondaryCache(name, expire, path);
            if (contents != null) return contents;

            // fetch
            return fetch(name, path, url);

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
        return "1.0.1";
    }
}
