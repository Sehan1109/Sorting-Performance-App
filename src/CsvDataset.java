// MEMBER 1 — CSV UPLOAD AND DATASET HANDLING
// Original application logic and UI implementation preserved.

import java.util.ArrayList;
import java.util.List;

/* ========= CsvDataset.java ========= */
/**
 * Holds a parsed CSV file: header names and raw string rows.
 * Provides helpers to detect numeric columns and extract a column as double[].
 */
public class CsvDataset {

    private final List<String> headers;
    private final List<String[]> rows;

    public CsvDataset(List<String> headers, List<String[]> rows) {
        this.headers = headers;
        this.rows = rows;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public int getRowCount() {
        return rows.size();
    }

    /** A column is numeric if it has at least one value and every non-empty cell parses as a number. */
    public boolean isNumericColumn(int col) {
        int numericCount = 0;
        for (String[] row : rows) {
            String cell = cell(row, col);
            if (cell.isEmpty()) continue;
            if (parseNumber(cell) == null) return false;
            numericCount++;
        }
        return numericCount > 0;
    }

    /** Result of extracting a column: the numeric values plus how many cells were skipped (empty). */
    public static class ColumnData {
        public final double[] values;
        public final int skipped;

        ColumnData(double[] values, int skipped) {
            this.values = values;
            this.skipped = skipped;
        }
    }

    /**
     * Extracts a numeric column. Empty cells are skipped.
     * @throws IllegalArgumentException if the column is not numeric or contains no data.
     */
    public ColumnData extractNumericColumn(int col) {
        double[] buffer = new double[rows.size()];
        int n = 0;
        int skipped = 0;
        for (String[] row : rows) {
            String cell = cell(row, col);
            if (cell.isEmpty()) {
                skipped++;
                continue;
            }
            Double d = parseNumber(cell);
            if (d == null) {
                throw new IllegalArgumentException("Column '" + headers.get(col)
                        + "' is not numeric (found value: \"" + cell + "\"). Please select a numeric column.");
            }
            buffer[n++] = d;
        }
        if (n == 0) {
            throw new IllegalArgumentException("Column '" + headers.get(col) + "' is empty.");
        }
        double[] values = new double[n];
        System.arraycopy(buffer, 0, values, 0, n);
        return new ColumnData(values, skipped);
    }

    private static String cell(String[] row, int col) {
        return col < row.length && row[col] != null ? row[col].trim() : "";
    }

    private static Double parseNumber(String s) {
        try {
            String cleaned = s.replace(",", "");   // tolerate thousands separators like "1,200"
            double d = Double.parseDouble(cleaned);
            if (Double.isNaN(d) || Double.isInfinite(d)) return null;
            return d;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Convenience for building a dataset from a raw list of rows where row 0 is the header. */
    public static CsvDataset fromRawRows(List<String[]> raw) {
        List<String> headers = new ArrayList<>();
        for (String h : raw.get(0)) headers.add(h.trim());
        return new CsvDataset(headers, new ArrayList<>(raw.subList(1, raw.size())));
    }
}

