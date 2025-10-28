import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введіть мінімальне значення діапазону (-100): ");
        int min = scanner.nextInt();

        System.out.print("Введіть максимальне значення діапазону (100): ");
        int max = scanner.nextInt();

        System.out.print("Введіть множник: ");
        int multiplier = scanner.nextInt();

        int size = new Random().nextInt(21) + 40;
        int[] numbers = new int[size];

        Random rand = new Random();
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = rand.nextInt(max - min + 1) + min;
        }

        System.out.println("\nПочатковий масив:");
        System.out.println(Arrays.toString(numbers));

        long startTime = System.currentTimeMillis();

        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<List<Integer>>> futures = new ArrayList<>();
        CopyOnWriteArrayList<Integer> resultList = new CopyOnWriteArrayList<>();

        int chunkSize = (int) Math.ceil((double) numbers.length / threadCount);
        for (int i = 0; i < numbers.length; i += chunkSize) {
            int start = i;
            int end = Math.min(i + chunkSize, numbers.length);
            int[] part = Arrays.copyOfRange(numbers, start, end);

            Callable<List<Integer>> task = () -> {
                List<Integer> partResult = new ArrayList<>();
                for (int n : part) {
                    partResult.add(n * multiplier);
                    Thread.sleep(20);
                }
                return partResult;
            };

            futures.add(executor.submit(task));
        }

        boolean allDone = false;
        while (!allDone) {
            allDone = true;
            for (Future<List<Integer>> future : futures) {
                if (!future.isDone()) {
                    allDone = false;
                    break;
                }
            }
            if (!allDone) {
                System.out.println("Очікування завершення всіх потоків...");
                Thread.sleep(50);
            }
        }

        for (Future<List<Integer>> future : futures) {
            try {
                resultList.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        long endTime = System.currentTimeMillis();
        double seconds = (endTime - startTime) / 1000.0;

        System.out.println("\nОброблений масив:");
        System.out.println(resultList);
        System.out.println("\nЧас виконання програми: " + seconds + " секунд");

        boolean anyCancelled = futures.stream().anyMatch(Future::isCancelled);
        System.out.println("\nisDone: true, isCancelled: " + anyCancelled);
    }
}
