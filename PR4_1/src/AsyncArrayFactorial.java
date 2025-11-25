import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class AsyncArrayFactorial {

    public static BigInteger factorial(long n) {
        BigInteger result = BigInteger.ONE;
        for (long i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }

    public static void main(String[] args) {

        CompletableFuture<int[]> generateArray = CompletableFuture.supplyAsync(() -> {
            long startTime = System.nanoTime();
            Random random = new Random();
            int[] array = new int[10];
            for (int i = 0; i < array.length; i++) {
                array[i] = random.nextInt(100);
            }
            long endTime = System.nanoTime();
            System.out.println("Початковий масив: " + Arrays.toString(array));
            System.out.printf("Час генерації масиву: %.3f мс%n", (endTime - startTime) / 1_000_000.0);
            return array;
        });

        CompletableFuture<int[]> incrementArray = generateArray.thenApplyAsync(array -> {
            long startTime = System.nanoTime();
            int[] newArray = Arrays.copyOf(array, array.length);
            for (int i = 0; i < newArray.length; i++) {
                newArray[i] += 5;
            }
            long endTime = System.nanoTime();
            System.out.println("Масив після збільшення на 5: " + Arrays.toString(newArray));
            System.out.printf("Час збільшення елементів: %.3f мс%n", (endTime - startTime) / 1_000_000.0);
            return newArray;
        });

        incrementArray.thenAcceptAsync(arr -> {
            long startTime = System.nanoTime();
            System.out.println("thenAcceptAsync: масив оброблено без повернення нового масиву");
            long endTime = System.nanoTime();
            System.out.printf("Час виконання thenAcceptAsync: %.3f мс%n", (endTime - startTime) / 1_000_000.0);
        });

        CompletableFuture<Void> factorialCalculation = incrementArray.thenCombineAsync(
                generateArray,
                (incremented, original) -> {
                    long startTime = System.nanoTime();
                    long sumIncremented = Arrays.stream(incremented).sum();
                    long sumOriginal = Arrays.stream(original).sum();
                    long totalSum = sumIncremented + sumOriginal;
                    BigInteger fact = factorial(totalSum);
                    long endTime = System.nanoTime();
                    System.out.println("Сума початкового масиву: " + sumOriginal);
                    System.out.println("Сума збільшеного масиву: " + sumIncremented);
                    System.out.println("Факторіал від суми: " + fact);
                    System.out.printf("Час обчислення факторіалу: %.3f мс%n", (endTime - startTime) / 1_000_000.0);
                    return null;
                }
        );

        CompletableFuture<Void> runExample = CompletableFuture.runAsync(() -> {
            long startTime = System.nanoTime();
            System.out.println("runAsync: виконана проста асинхронна операція");
            long endTime = System.nanoTime();
            System.out.printf("Час виконання runAsync: %.3f мс%n", (endTime - startTime) / 1_000_000.0);
        });

        factorialCalculation.thenRunAsync(() -> System.out.println("Усі асинхронні операції завершені."));

        factorialCalculation.join();
        runExample.join();
    }
}
