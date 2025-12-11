import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class Route {
    String transport;
    double price;
    double time;

    Route(String transport, double price, double time) {
        this.transport = transport;
        this.price = price;
        this.time = time;
    }

    @Override
    public String toString() {
        return transport + " (Ціна: " + price + "грн, Час: " + time + " год)";
    }
}

public class TravelPlanner {


    public static CompletableFuture<Route> getRoute(String transport) {
        return CompletableFuture.supplyAsync(() -> {
            try {

                Thread.sleep((long) (Math.random() * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            switch (transport) {
                case "Train": return new Route("Поїзд", 900, 5);
                case "Bus": return new Route("Автобус", 600, 7);
                case "Plane": return new Route("Літак", 3000, 2);
                default: return new Route("Невідомий", 0, 0);
            }
        });
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        CompletableFuture<Route> train = getRoute("Train");
        CompletableFuture<Route> bus = getRoute("Bus");
        CompletableFuture<Route> plane = getRoute("Plane");


        CompletableFuture<Void> all = CompletableFuture.allOf(train, bus, plane);
        all.join();

        Route trainRoute = train.get();
        Route busRoute = bus.get();
        Route planeRoute = plane.get();

        System.out.println("Усі маршрути отримано:");
        System.out.println(trainRoute);
        System.out.println(busRoute);
        System.out.println(planeRoute);


        CompletableFuture<Object> first = CompletableFuture.anyOf(train, bus, plane);
        System.out.println("\nПерший готовий маршрут: " + first.get());


        CompletableFuture<Route> cheaper = train.thenCombine(bus, (r1, r2) -> r1.price < r2.price ? r1 : r2);
        System.out.println("\nДешевший між Поїздом та Автобусом: " + cheaper.get());


        CompletableFuture<Route> optimalRoute = bus.thenCombine(plane, (r1, r2) -> r1.time < r2.time ? r1 : r2)
                .thenCompose(fastest -> train.thenApply(r -> {

                    double scoreFastest = fastest.price + fastest.time;
                    double scoreTrain = r.price + r.time;
                    return scoreFastest < scoreTrain ? fastest : r;
                }));
        System.out.println("\nОптимальний маршрут (ціна та час): " + optimalRoute.get());
    }
}
