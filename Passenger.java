import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Passenger extends Entity {

    private static final Map<String, Passenger> passengers = new HashMap<>();

    private Passenger(String name) {
        super(name);
    }

    public static Passenger make(String name) {
        if(!passengers.containsKey(name)) {
            passengers.put(name, new Passenger(name));
        }
        return passengers.get(name);
    }

    public static void clearCache() {
        passengers.clear();
    }
}
