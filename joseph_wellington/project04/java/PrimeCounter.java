import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class PrimeCounter {

    static boolean isPrime(long n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (long i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    static List<Long> readNumbers(String path) throws IOException {
        List<Long> nums = new ArrayList<>();
        for (String line : Files.readAllLines(Paths.get(path))) {
            line = line.trim();
            if (line.isEmpty()) continue;
            try {
                nums.add(Long.parseLong(line));
            } catch (NumberFormatException ignored) {
                // skip invalid lines gracefully
            }
        }
        return nums;
    }

    static long countSingle(List<Long> nums) {
        long count = 0;
        for (long n : nums) if (isPrime(n)) count++;
        return count;
    }

    static long countMulti(List<Long> nums) throws Exception {
        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        int n = nums.size();
        int chunk = (n + threads - 1) / threads;

        List<Future<Long>> futures = new ArrayList<>();

        for (int t = 0; t < threads; t++) {
            final int start = t * chunk;
            final int end = Math.min(n, start + chunk);
            if (start >= end) break;

            futures.add(pool.submit(() -> {
                long c = 0;
                for (int i = start; i < end; i++) {
                    if (isPrime(nums.get(i))) c++;
                }
                return c;
            }));
        }

        long total = 0;
        for (Future<Long> f : futures) total += f.get();

        pool.shutdown();
        return total;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java PrimeCounter <file>");
            return;
        }
        String file = args[0];
        List<Long> nums = readNumbers(file);

        // IMPORTANT: time ONLY computation, not file reading
        long t1 = System.nanoTime();
        long single = countSingle(nums);
        long t2 = System.nanoTime();

        long t3 = System.nanoTime();
        long multi = countMulti(nums);
        long t4 = System.nanoTime();

        double singleMs = (t2 - t1) / 1_000_000.0;
        double multiMs  = (t4 - t3) / 1_000_000.0;

        System.out.println("Single-threaded primes: " + single);
        System.out.println("Single time ms: " + singleMs);

        System.out.println("Multi-threaded primes: " + multi);
        System.out.println("Multi time ms: " + multiMs);

        if (multiMs > 0) System.out.println("Speedup: " + (singleMs / multiMs));
    }
}


