/*
 * Copyright (c) 2018 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.dalesbred.result;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.nCopies;
import static java.util.stream.Collectors.toList;
import static org.dalesbred.internal.utils.StringUtils.rightPad;
import static org.dalesbred.internal.utils.StringUtils.truncate;

final class TableFormatter {

    private static final int MAX_COLUMN_LENGTH = 50;

    private TableFormatter() {
    }

    static @NotNull String toString(@NotNull ResultTable table) {
        try {
            StringBuilder sb = new StringBuilder(100 * table.getRowCount());
            write(table, sb);
            return sb.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void write(@NotNull ResultTable table, @NotNull Appendable out) throws IOException {
        List<List<String>> formattedValues = format(table);
        int[] columnLengths = columnLengths(table.getColumnNames(), formattedValues);

        writeRow(out, table.getColumnNames(), columnLengths, ' ');
        writeRow(out, nCopies(table.getColumnCount(), ""), columnLengths, '-');

        for (List<String> row : formattedValues)
            writeRow(out, row, columnLengths, ' ');
    }

    private static void writeRow(@NotNull Appendable out, @NotNull List<String> values, int[] columnLengths, char padding) throws IOException {
        assert values.size() == columnLengths.length;

        out.append('|');
        for (int i = 0; i < columnLengths.length; i++)
            out.append(' ').append(formatCell(values.get(i), columnLengths[i], padding)).append(" |");

        out.append('\n');
    }

    private static @NotNull List<List<String>> format(@NotNull ResultTable table) {
        return table.getRows().stream()
                .map(it -> it.asList().stream().map(String::valueOf).collect(toList()))
                .collect(toList());
    }

    private static @NotNull String formatCell(@NotNull String v, int length, char padding) {
        return rightPad(truncate(v, length), length, padding);
    }

    private static int[] columnLengths(@NotNull List<String> columnNames, @NotNull List<List<String>> formattedValues) {
        int[] columnLengths = new int[columnNames.size()];

        for (int i = 0; i < columnLengths.length; i++)
            columnLengths[i] = columnNames.get(i).length();

        for (List<String> row : formattedValues)
            for (int i = 0; i < columnLengths.length; i++)
                columnLengths[i] = max(columnLengths[i], row.get(i).length());

        for (int i = 0; i < columnLengths.length; i++)
            columnLengths[i] = min(columnLengths[i], MAX_COLUMN_LENGTH);

        return columnLengths;
    }
}
