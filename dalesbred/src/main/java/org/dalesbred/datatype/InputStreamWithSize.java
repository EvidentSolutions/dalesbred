package org.dalesbred.datatype;

import org.jetbrains.annotations.NotNull;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 * InputStream that also knows the length of its input. When this kind of stream is
 * passed in place of normal stream, the database can optimize its work better.
 */
public final class InputStreamWithSize extends FilterInputStream {

    private final long size;

    public InputStreamWithSize(@NotNull InputStream in, long size) {
        super(in);

        if (size < 0) throw new IllegalArgumentException("negative size: " + size);

        this.size = size;
    }

    public long getSize() {
        return size;
    }
}
