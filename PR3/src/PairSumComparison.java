import java.util.*;
import java.util.concurrent.*;

public class PairSumComparison {

    static class PairSumTask extends RecursiveTask<Long> {
        private final int[] arr;
        private final int pairStart, pairEnd;
        private static final int PAIR_THRESHOLD = 2000;

        public PairSumTask(int[] arr, int pairStart, int pairEnd) {
            this.arr = arr;
            this.pairStart = pairStart;
            this.pairEnd = pairEnd;
        }

        @Override
        protected Long compute() {
            int pairs = pairEnd - pairStart;
            if (pairs <= PAIR_THRESHOLD) {
                long sum = 0;
                for (int p = pairStart; p < pairEnd; p++) {
                    sum += arr[2 * p] + arr[2 * p + 1];
                }
                return sum;
            } else {
                int mid = (pairStart + pairEnd) >>> 1;
                PairSumTask left = new PairSumTask(arr, pairStart, mid);
                PairSumTask right = new PairSumTask(arr, mid, pairEnd);
                left.fork();
                long rightResult = right.compute();
                long leftResult = left.join();
                return leftResult + rightResult;
            }
        }
    }

    static long workDealingSum(int[] arr, int pairsCount) throws InterruptedException, ExecutionException {
        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<Long>> futures = new ArrayList<>();

        int chunkSize = pairsCount / threads;
        for (int t = 0; t < threads; t++) {
            int start = t * chunkSize;
            int end = (t == threads - 1) ? pairsCount : start + chunkSize;

            futures.add(executor.submit(() -> {
                long sum = 0;
                for (int i = start; i < end; i++) {
                    sum += arr[2 * i] + arr[2 * i + 1];
                }
                return sum;
            }));
        }

        long totalSum = 0;
        for (Future<Long> f : futures) totalSum += f.get();
        executor.shutdown();
        return totalSum;
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Введіть кількість елементів масиву: ");
        int n = sc.nextInt();
        System.out.print("Введіть мінімальне значення: ");
        int min = sc.nextInt();
        System.out.print("Введіть максимальне значення: ");
        int max = sc.nextInt();

        if (n < 2) {
            System.out.println("Потрібно щонайменше 2 елементи.");
            return;
        }

        int[] arr = new int[n];
        Random rand = new Random();
        for (int i = 0; i < n; i++) arr[i] = rand.nextInt(max - min + 1) + min;

        System.out.println("\nЗгенерований масив:");
        for (int i = 0; i < Math.min(n, 20); i++) System.out.print(arr[i] + " ");
        if (n > 20) System.out.print("...");
        System.out.println();

        int pairsCount = n / 2;

        ForkJoinPool pool = new ForkJoinPool();
        long startTime = System.nanoTime();
        long resultStealing = pool.invoke(new PairSumTask(arr, 0, pairsCount));
        long endTime = System.nanoTime();
        System.out.println("\nWork Stealing:");
        System.out.println("Сума пар: " + resultStealing);
        System.out.println("Час виконання: " + (endTime - startTime) / 1_000_000 + " мс");

        startTime = System.nanoTime();
        long resultDealing = workDealingSum(arr, pairsCount);
        endTime = System.nanoTime();
        System.out.println("\nWork Dealing:");
        System.out.println("Сума пар: " + resultDealing);
        System.out.println("Час виконання: " + (endTime - startTime) / 1_000_000 + " мс");

        if (n % 2 == 1) {
            System.out.println("\nУвага: останній елемент (" + arr[n - 1] + ") ігноровано (непарна кількість елементів).");
        }
    }
}
