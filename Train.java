import java.util.HashMap;
import java.util.Map;

public class Train extends Entity {

    private static final Map<String, Train> trains = new HashMap<>();

    private Train(String name) {
        super(name);
    }

    public static Train make(String name) {
        if(!trains.containsKey(name)) {
            trains.put(name, new Train(name));
        }
        return trains.get(name);
    }

    public static void clearCache() {
        trains.clear();
    }
}
