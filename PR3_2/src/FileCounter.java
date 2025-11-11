import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class FileCounter {

    static class CountFilesTask extends RecursiveTask<Integer> {
        private final File dir;
        private final String extension;

        public CountFilesTask(File dir, String extension) {
            this.dir = dir;
            this.extension = extension;
        }

        @Override
        protected Integer compute() {
            int count = 0;
            File[] files = dir.listFiles();

            if (files != null) {
                List<CountFilesTask> subTasks = new ArrayList<>();
                for (File file : files) {
                    if (file.isDirectory() && !file.isHidden()) {
                        CountFilesTask task = new CountFilesTask(file, extension);
                        task.fork();
                        subTasks.add(task);
                    } else if (file.isFile() && !file.isHidden()) {
                        int dotIndex = file.getName().lastIndexOf('.');
                        if (dotIndex != -1) {
                            String ext = file.getName().substring(dotIndex);
                            if (ext.equalsIgnoreCase(extension)) {
                                count++;
                            }
                        }
                    }
                }
                for (CountFilesTask task : subTasks) {
                    count += task.join();
                }
            }
            return count;
        }
    }

    static class CountFilesRunnable implements Callable<Integer> {
        private final File dir;
        private final String extension;

        public CountFilesRunnable(File dir, String extension) {
            this.dir = dir;
            this.extension = extension;
        }

        @Override
        public Integer call() throws Exception {
            int count = 0;
            File[] files = dir.listFiles();

            if (files != null) {
                List<Future<Integer>> futures = new ArrayList<>();
                ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

                for (File file : files) {
                    if (file.isDirectory() && !file.isHidden()) {
                        futures.add(executor.submit(new CountFilesRunnable(file, extension)));
                    } else if (file.isFile() && !file.isHidden()) {
                        int dotIndex = file.getName().lastIndexOf('.');
                        if (dotIndex != -1) {
                            String ext = file.getName().substring(dotIndex);
                            if (ext.equalsIgnoreCase(extension)) {
                                count++;
                            }
                        }
                    }
                }

                for (Future<Integer> future : futures) {
                    count += future.get();
                }

                executor.shutdown();
            }

            return count;
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введіть шлях до директорії: ");
        String directoryPath = scanner.nextLine().trim();

        System.out.print("Введіть формат файлів (наприклад, .pdf): ");
        String extension = scanner.nextLine().trim();

        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Вказана директорія не існує!");
            return;
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        CountFilesTask forkJoinTask = new CountFilesTask(dir, extension);
        int forkJoinResult = forkJoinPool.invoke(forkJoinTask);

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CountFilesRunnable callableTask = new CountFilesRunnable(dir, extension);
        Future<Integer> futureResult = executor.submit(callableTask);
        int threadPoolResult = futureResult.get();
        executor.shutdown();


        int totalFiles = forkJoinResult;
        System.out.println("Кількість файлів з розширенням " + extension + ": " + totalFiles);
    }
}
