// Member 2
// ALGORITHM TIMING, RANKING, RESULTS AND SELF TEST 
import java.util.*;
import java.util.function.Consumer;

/**
 * Runs every sorting algorithm on its own copy of the data, measures execution time
 * with System.nanoTime(), verifies the output and identifies the fastest algorithm.
 */
class PerformanceEvaluator {

    private final Map<String, Consumer<double[]>> algorithms = new LinkedHashMap<>();

    public PerformanceEvaluator() {
        algorithms.put("Insertion Sort", SortingAlgorithms::insertionSort);
        algorithms.put("Shell Sort", SortingAlgorithms::shellSort);
        algorithms.put("Merge Sort", SortingAlgorithms::mergeSort);
        algorithms.put("Quick Sort", SortingAlgorithms::quickSort);
        algorithms.put("Heap Sort", SortingAlgorithms::heapSort);
    }

    public List<String> getAlgorithmNames() {
        return new ArrayList<>(algorithms.keySet());
    }

    /** Listener notified before each algorithm starts (used for progress updates). */
    public interface ProgressListener {
        void onStart(String algorithm, int index, int total);

        /** Called when an algorithm has finished (optional). */
        default void onFinish(SortResult result) { }
    }

    public List<SortResult> evaluate(double[] original, ProgressListener listener) {
        List<SortResult> results = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Consumer<double[]>> entry : algorithms.entrySet()) {
            if (listener != null) listener.onStart(entry.getKey(), index, algorithms.size());
            double[] copy = original.clone();          // each algorithm gets identical input

            long start = System.nanoTime();
            entry.getValue().accept(copy);
            long end = System.nanoTime();

            double millis = (end - start) / 1_000_000.0;
            SortResult result = new SortResult(entry.getKey(), millis, SortingAlgorithms.isSorted(copy), copy);
            results.add(result);
            if (listener != null) listener.onFinish(result);
            index++;
        }
        return results;
    }

    /** Returns the correctly sorted result with the shortest time, or null if none are valid. */
    public static SortResult findBest(List<SortResult> results) {
        SortResult best = null;
        for (SortResult r : results) {
            if (!r.isSortedCorrectly()) continue;
            if (best == null || r.getTimeMillis() < best.getTimeMillis()) best = r;
        }
        return best;
    }
}

/** Outcome of running one sorting algorithm: its name, execution time and correctness check. */
class SortResult {
    private final String algorithm;
    private final double timeMillis;
    private final boolean sortedCorrectly;
    private final double[] sortedData;

    public SortResult(String algorithm, double timeMillis, boolean sortedCorrectly, double[] sortedData) {
        this.algorithm = algorithm;
        this.timeMillis = timeMillis;
        this.sortedCorrectly = sortedCorrectly;
        this.sortedData = sortedData;
    }

    public String getAlgorithm() { return algorithm; }
    public double getTimeMillis() { return timeMillis; }
    public boolean isSortedCorrectly() { return sortedCorrectly; }
    public double[] getSortedData() { return sortedData; }
}

/** Console self test (no GUI): checks every algorithm against Arrays.sort on many inputs. */
class AlgorithmSelfTest {
    public static void main(String[] args) {
        PerformanceEvaluator ev = new PerformanceEvaluator();
        Random rnd = new Random(42);
        int failures = 0;
        int[] sizes = {0, 1, 2, 3, 10, 100, 1000, 5000};
        for (int n : sizes) {
            double[][] inputs = new double[5][n];
            for (int i = 0; i < n; i++) {
                inputs[0][i] = rnd.nextInt(1000) - 500;       // random with duplicates/negatives
                inputs[1][i] = i;                              // already sorted
                inputs[2][i] = n - i;                          // reverse sorted
                inputs[3][i] = 7;                              // all equal
                inputs[4][i] = rnd.nextDouble() * 1e6;         // random decimals
            }
            for (double[] in : inputs) {
                double[] expected = in.clone();
                Arrays.sort(expected);
                for (SortResult r : ev.evaluate(in, null)) {
                    if (!Arrays.equals(expected, r.getSortedData())) {
                        failures++;
                        System.out.println("FAIL: " + r.getAlgorithm() + " n=" + n);
                    }
                }
            }
        }
        System.out.println(failures == 0 ? "All algorithm tests passed." : failures + " failures.");

        String[] parsed = CsvLoader.parseLine("1,\"Smith, John\",\"say \"\"hi\"\"\",,42.5");
        System.out.println("CSV parse OK: " + (parsed.length == 5 && parsed[1].equals("Smith, John")
                && parsed[2].equals("say \"hi\"") && parsed[3].isEmpty()));
    }
}
