import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AsyncMinPairSum {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();

        CompletableFuture<int[]> generateSequence = CompletableFuture.supplyAsync(() -> {
            System.out.println("Генеруємо випадкову послідовність з 20 елементів...");
            Random rand = new Random();
            int[] sequence = new int[20];
            for (int i = 0; i < sequence.length; i++) {
                sequence[i] = rand.nextInt(100) + 1;
            }
            return sequence;
        });

        CompletableFuture<int[]> printSequence = generateSequence.thenApplyAsync(seq -> {
            System.out.println("Початкова послідовність: " + Arrays.toString(seq));
            return seq;
        });

        CompletableFuture<Integer> minPairSum = printSequence.thenApplyAsync(seq -> {
            System.out.println("Обчислюємо мінімум сум сусідніх елементів...");
            int minSum = seq[0] + seq[1];
            for (int i = 1; i < seq.length - 1; i++) {
                int sum = seq[i] + seq[i + 1];
                if (sum < minSum) {
                    minSum = sum;
                }
            }
            return minSum;
        });

        CompletableFuture<Void> printResult = minPairSum.thenAcceptAsync(result ->
                System.out.println("Мінімальна сума сусідніх елементів: " + result)
        );

        CompletableFuture<Void> runAsyncDemo = CompletableFuture.runAsync(() ->
                System.out.println("Асинхронна дія без повернення результату (runAsync)")
        );
        runAsyncDemo.get();

        CompletableFuture<Void> finalMessage = printResult.thenRunAsync(() -> {
            long endTime = System.currentTimeMillis();
            System.out.println("Всі асинхронні операції виконано. Час роботи: " + (endTime - startTime) + " мс");
        });

        finalMessage.get();
    }
}
