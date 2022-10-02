package net.toshimichi.fetch;

import java.time.LocalDateTime;

public class CacheData {

    private final String contents;
    private final LocalDateTime lastModified;

    public CacheData(String contents, LocalDateTime lastModified) {
        this.contents = contents;
        this.lastModified = lastModified;
    }

    public String getContents() {
        return contents;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }
}
