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
