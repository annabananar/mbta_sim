import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Station extends Entity {

    private final Lock trainLock;
    private final Lock passengerLock;
    private final Condition availability;
    private final Condition trainArrival;
    private volatile boolean isOccupied;

    private static final Map<String, Station> stations = new HashMap<>();

    private Station(String name) {
        super(name);
        trainLock = new ReentrantLock();
        passengerLock = new ReentrantLock();
        availability = trainLock.newCondition();
        trainArrival = passengerLock.newCondition();
        isOccupied = false;
    }

    public static Station make(String name) {
        if(!stations.containsKey(name)) {
            stations.put(name, new Station(name));
        }
        return stations.get(name);
    }

    // called by a train when it leaves this station
    public void notifyDeparture() {
        trainLock.lock();
        try {
            isOccupied = false;
            System.out.println("isOccupied at " + this + " set to false by " + Thread.currentThread().getName());
            availability.signalAll();
        } finally {
            trainLock.unlock();
        }
    }

    // called by trains when they are moving to this station
    public void claimOrWait() throws InterruptedException {
        /* trains planning to enter the station line up to get the lock */
        trainLock.lock();
        try {
            /* some train gets the lock but another train may still be there */
            while(isOccupied) {
                availability.await();       /* thread joins waitlist again, trainLock gets released here */
            }
            /* thread gets here if isOccupied is false */
            isOccupied = true;
            System.out.println("isOccupied at " +  this + "set to true by " + Thread.currentThread().getName());
        } finally {
            /* train threads releases trainLock upon arrival */
            trainLock.unlock();
        }
    }

    public void signalPassengersAtSta() {
        passengerLock.lock();
        try {
            trainArrival.signalAll();
        } finally {
            passengerLock.unlock();
        }
    }

    public void awaitTrains() throws InterruptedException {
        passengerLock.lock();
        try {
            while(!isOccupied) {
                trainArrival.await();      // subscribes current passenger thread to listen to train arrivals
            }
        } finally {
            passengerLock.unlock();
        }
    }

    public static void clearCache() {
        stations.clear();
    }
}
