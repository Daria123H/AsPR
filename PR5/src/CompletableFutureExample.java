import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableFutureExample {

    // Імітація отримання даних з першого джерела
    public static CompletableFuture<String> getDataFromSource1() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000); // імітуємо затримку
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Data from Source 1";
        });
    }

    // Імітація отримання даних з другого джерела
    public static CompletableFuture<String> getDataFromSource2() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1500); // імітуємо затримку
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Data from Source 2";
        });
    }

    // Імітація отримання даних з третього джерела
    public static CompletableFuture<String> getDataFromSource3() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1200); // імітуємо затримку
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Data from Source 3";
        });
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // Використання thenCompose() - послідовна обробка
        CompletableFuture<String> composed = getDataFromSource1()
                .thenCompose(data1 -> getDataFromSource2()
                        .thenApply(data2 -> data1 + " + " + data2));
        System.out.println("thenCompose result: " + composed.get());

        // Використання thenCombine() - комбінування результатів двох асинхронних задач
        CompletableFuture<String> combined = getDataFromSource1()
                .thenCombine(getDataFromSource2(), (data1, data2) -> data1 + " & " + data2);
        System.out.println("thenCombine result: " + combined.get());

        // Використання allOf() - чекаємо завершення всіх задач
        CompletableFuture<Void> all = CompletableFuture.allOf(
                getDataFromSource1(),
                getDataFromSource2(),
                getDataFromSource3()
        );

        all.thenRun(() -> System.out.println("All tasks completed"));

        // Використання anyOf() - чекаємо завершення будь-якої задачі
        CompletableFuture<Object> any = CompletableFuture.anyOf(
                getDataFromSource1(),
                getDataFromSource2(),
                getDataFromSource3()
        );

        any.thenAccept(result -> System.out.println("First completed task result: " + result));

        // Щоб програма не завершилась раніше, чекаємо завершення всіх
        all.get();
        any.get();
    }
}
