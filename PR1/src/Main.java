import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class Order {
    int id;
    int quantity;

    Order(int id, int quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Замовлення #" + id + " на " + quantity + " товарів";
    }
}

public class Main {
    private static boolean isOpen = true;
    private static int stock = 15;
    private static final BlockingQueue<Order> orders = new LinkedBlockingQueue<>();
    private static final Random random = new Random();
    private static boolean shortageHandledToday = false;

    public static void main(String[] args) {
        // Завдання для покупця
        Runnable customerTask = () -> {
            int orderId = 1;
            while (true) {
                try {
                    Thread.sleep(2500);
                    int quantity = random.nextInt(5) + 1;
                    Order order = new Order(orderId++, quantity);
                    System.out.println("[Покупець] Зробив " + order);
                    orders.put(order);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // Завдання для адміністратора
        Runnable adminTask = () -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                    if (isOpen) {
                        for (int i = 0; i < 3; i++) {
                            Order order = orders.poll();
                            if (order == null) break;

                            synchronized (Main.class) {
                                if (stock >= order.quantity) {
                                    stock -= order.quantity;
                                    System.out.println("[Адміністратор] Виконав " + order + ". Залишок товару: " + stock);
                                } else {
                                    if (!shortageHandledToday) {
                                        shortageHandledToday = true;
                                        int added = random.nextInt(10) + 10;
                                        stock += added;
                                        System.out.println("[Адміністратор] Не вистачало товару для " + order +
                                                ". Додав " + added + " одиниць. Всього: " + stock);
                                        stock -= order.quantity;
                                        System.out.println("[Адміністратор] Виконав " + order + " після поповнення. Залишок: " + stock);
                                    } else {
                                        stock -= order.quantity;
                                        System.out.println("[Адміністратор] Виконав " + order + ". Залишок: " + stock);
                                    }
                                }
                            }
                        }
                        // Додавання товару для запасу
                        int added = stock < 10 ? random.nextInt(5) + 1 : random.nextInt(3) + 1;
                        stock += added;
                        System.out.println("[Адміністратор] Поповнив запас на " + added + ". Всього: " + stock);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // Завдання для магазину (відкрито/зачинено)
        Runnable shopTask = () -> {
            while (true) {
                try {
                    isOpen = true;
                    shortageHandledToday = false;
                    System.out.println("[Магазин] Відкрито! Можна робити покупки.");
                    Thread.sleep(15000);

                    isOpen = false;
                    System.out.println("[Магазин] Зачинено!");
                    Thread.sleep(8000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // Створення потоків
        Thread customerThread = new Thread(customerTask);
        Thread adminThread = new Thread(adminTask);
        Thread shopThread = new Thread(shopTask);

        customerThread.start();
        adminThread.start();
        shopThread.start();

        // Вивід "стану" потоків у зрозумілій формі
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000);
                    System.out.println("\nПоточний стан потоків");
                    System.out.println("[Покупець] " + getUserFriendlyState(customerThread));
                    System.out.println("[Адміністратор] " + getUserFriendlyState(adminThread));
                    System.out.println("[Магазин] " + getUserFriendlyState(shopThread));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Метод для зрозумілого відображення стану потоку
    private static String getUserFriendlyState(Thread thread) {
        Thread.State state = thread.getState();
        return switch (state) {
            case NEW -> "Ще не почав працювати";
            case RUNNABLE -> "Виконується зараз або готовий до дії";
            case TIMED_WAITING -> "Чекає трохи перед наступною дією";
            case WAITING -> "Чекає на щось";
            case BLOCKED -> "Тимчасово не може працювати через іншу задачу";
            case TERMINATED -> "Завершив роботу";
        };
    }
}
