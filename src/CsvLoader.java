// MEMBER 1 - FILE VALIDATION AND CSV LOADING (2 of 2)
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/* ========= CsvLoader.java ========= */
/**
 * Reads a .csv file into a {@link CsvDataset}.
 * Supports quoted fields (including commas and escaped quotes "") and a UTF-8 BOM.
 */
class CsvLoader {

    public static CsvDataset load(File file) throws IOException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IOException("The selected file does not exist.");
        }
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            throw new IOException("Invalid file type. Please select a .csv file.");
        }

        List<String[]> raw = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) {
                    line = stripBom(line);
                    first = false;
                }
                if (line.trim().isEmpty()) continue;   // skip blank lines
                raw.add(parseLine(line));
            }
        } catch (java.nio.charset.MalformedInputException e) {
            throw new IOException("The file is not valid UTF-8 text. Is it really a CSV file?");
        }

        if (raw.isEmpty()) {
            throw new IOException("The file is empty.");
        }
        if (raw.size() < 2) {
            throw new IOException("The file contains a header but no data rows.");
        }
        return CsvDataset.fromRawRows(raw);
    }

    private static String stripBom(String s) {
        return (!s.isEmpty() && s.charAt(0) == '\uFEFF') ? s.substring(1) : s;
    }

    /** Splits one CSV line into fields, respecting double quotes. */
    static String[] parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');   // escaped quote
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}
