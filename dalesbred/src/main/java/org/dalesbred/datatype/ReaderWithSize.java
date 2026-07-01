package org.dalesbred.datatype;

import org.jetbrains.annotations.NotNull;

import java.io.FilterReader;
import java.io.Reader;

/**
 * Reader that also knows the length of its input. When this kind of Reader is
 * passed in place of normal Reader, the database can optimize its work better.
 */
public final class ReaderWithSize extends FilterReader {

    private final long size;

    public ReaderWithSize(@NotNull Reader reader, long size) {
        super(reader);

        if (size < 0) throw new IllegalArgumentException("negative size: " + size);

        this.size = size;
    }

    public long getSize() {
        return size;
    }
}
