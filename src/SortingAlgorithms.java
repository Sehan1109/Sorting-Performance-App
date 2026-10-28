// Member 2
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implementations of the five required sorting algorithms.
 * Every method sorts the given array in place in ascending order.
 */
public final class SortingAlgorithms {

    private SortingAlgorithms() {}

    // ---------------------------------- Insertion Sort ------------------------------
    /** Best O(n), average/worst O(n^2). Stable, in place. */
    public static void insertionSort(double[] arr) {
        for (int i = 1; i < arr.length; i++) {
            double key = arr[i];
            int j = i - 1;
            // Shift elements greater than key one position to the right
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    // ---------------------------------------- Shell Sort -----------------------------
    /** Insertion sort over shrinking gaps (gap = n/2, n/4, ... 1). */
    public static void shellSort(double[] arr) {
        int n = arr.length;
        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                double temp = arr[i];
                int j = i;
                while (j >= gap && arr[j - gap] > temp) {
                    arr[j] = arr[j - gap];
                    j -= gap;
                }
                arr[j] = temp;
            }
        }
    }

    // ------------------------------------------- Merge Sort -------------------------------
    /** Divide and conquer, O(n log n) always. Stable, needs O(n) extra space. */
    public static void mergeSort(double[] arr) {
        if (arr.length < 2) return;
        double[] aux = new double[arr.length];
        mergeSort(arr, aux, 0, arr.length - 1);
    }

    private static void mergeSort(double[] arr, double[] aux, int left, int right) {
        if (left >= right) return;
        int mid = left + (right - left) / 2;
        mergeSort(arr, aux, left, mid);
        mergeSort(arr, aux, mid + 1, right);
        merge(arr, aux, left, mid, right);
    }

    private static void merge(double[] arr, double[] aux, int left, int mid, int right) {
        System.arraycopy(arr, left, aux, left, right - left + 1);
        int i = left, j = mid + 1, k = left;
        while (i <= mid && j <= right) {
            if (aux[i] <= aux[j]) arr[k++] = aux[i++];
            else arr[k++] = aux[j++];
        }
        while (i <= mid) arr[k++] = aux[i++];
        while (j <= right) arr[k++] = aux[j++];
    }

    // -------------------------------------------- Quick Sort -------------------------------
    /**
     * Average O(n log n). Uses a random pivot and recurses on the smaller partition
     * (looping on the larger one) so the recursion depth stays O(log n) and sorted
     * input does not cause a StackOverflowError.
     */
    public static void quickSort(double[] arr) {
        quickSort(arr, 0, arr.length - 1);
    }

    private static void quickSort(double[] arr, int low, int high) {
        while (low < high) {
            int p = partition(arr, low, high);
            if (p - low < high - p) {
                quickSort(arr, low, p - 1);
                low = p + 1;
            } else {
                quickSort(arr, p + 1, high);
                high = p - 1;
            }
        }
    }

    private static int partition(double[] arr, int low, int high) {
        int pivotIndex = ThreadLocalRandom.current().nextInt(low, high + 1);
        swap(arr, pivotIndex, high);
        double pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);
        return i + 1;
    }

    // ------------------------------------------- Heap Sort ----------------------------------
    /** Build a max-heap, then repeatedly move the root to the end. O(n log n), in place. */
    public static void heapSort(double[] arr) {
        int n = arr.length;
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i);
        }
        for (int end = n - 1; end > 0; end--) {
            swap(arr, 0, end);
            heapify(arr, end, 0);
        }
    }

    private static void heapify(double[] arr, int heapSize, int root) {
        while (true) {
            int largest = root;
            int left = 2 * root + 1;
            int right = 2 * root + 2;
            if (left < heapSize && arr[left] > arr[largest]) largest = left;
            if (right < heapSize && arr[right] > arr[largest]) largest = right;
            if (largest == root) return;
            swap(arr, root, largest);
            root = largest;
        }
    }

    // ---------------------------------------------------------------- Helpers
    private static void swap(double[] arr, int i, int j) {
        double t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }

    /** Returns true if the array is in non-decreasing order. Used to verify results. */
    public static boolean isSorted(double[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i - 1] > arr[i]) return false;
        }
        return true;
    }
}

