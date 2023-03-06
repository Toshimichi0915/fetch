package net.toshimichi.fetch;

import java.time.LocalDateTime;

public class CacheData {

    private final byte[] contents;
    private final LocalDateTime lastModified;

    public CacheData(byte[] contents, LocalDateTime lastModified) {
        this.contents = contents;
        this.lastModified = lastModified;
    }

    public byte[] getContents() {
        return contents;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }
}
