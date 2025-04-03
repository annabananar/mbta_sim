import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class TransitTrain implements TransitThread {

    private final Train t;
    private final MBTA mbta;
    private final Log log;

    private Station curr;

    public TransitTrain(Train t, MBTA mbta, Log log, Station tStart) {
        this.t = t;
        this.mbta = mbta;
        this.log = log;
        curr = tStart;
    }

    @Override
    public void run() {
        try {
            Lock currLock = mbta.getStationLock(curr);
            currLock.lock();
            while(!Thread.currentThread().isInterrupted()) {
                // System.out.println("Train " + t + " is at " + curr);
                Station next = mbta.getTNextStation(t);
                // System.out.println("Train " + t + " locks " + curr);

                /* tell passengers to board off/board on depending on their state */
                Condition pCondition = mbta.getPConditionAtS(curr);
                pCondition.signalAll();

                /* releases lock on current station before going into waiting state */
                currLock.unlock();
                // System.out.println("Train " + t + " unlocks " + curr + " going into sleep");

                /* wait for passenger threads to do their thing */
                Thread.sleep(10);
                // System.out.println("Train " + t + " wakes up");

                /* checks availability for the next station */
                Lock nextLock = mbta.getStationLock(next);
                nextLock.lock();
                Condition tConditionNext = mbta.getTConditionAtS(next);
                try {
                    // System.out.println("Train " + t + " locks " + next);
                    while(mbta.getTrainAtSta(next) != null) {
                        // System.out.println("Train " + t + " unlocks " + next);
                        tConditionNext.await();
                    }
                    /* train now has the lock for the next station */
                    log.train_moves(t, curr, next);
                    mbta.moveTrain(t, curr, next);
                    /* notifies all trains waiting to enter the current station */
                    currLock.lock();
                    try {
                        // System.out.println("Train " + t + " locks " + curr);
                        Condition tConditionCurr = mbta.getTConditionAtS(curr);
                        tConditionCurr.signalAll();
                    } finally {
                        // System.out.println("Train " + t + " unlocks on " + curr);
                        currLock.unlock();
                    }
                    curr = next;
                    currLock = mbta.getStationLock(next);
                } catch (InterruptedException e) {

                }
            }
        } catch (InterruptedException e) {
            return;
        }
    }

    /* subscribes passengers to listen to the next station this train arrives at
    public void waitForNext() throws InterruptedException {
        lock.lock();
        try {
            staArrival.await();
        } finally {
            lock.unlock();
        }
    }

    wakes up all passengers on train upon arriving at a new station
    public void notifyPassengers() throws InterruptedException {
        lock.lock();
        try {
            staArrival.signalAll();
        } finally {
            lock.unlock();
        }
    } */

    /* curr = mbta.getTCurrStation(t);
        next = mbta.getTNextStation(t);
        curr.claimOrWait();
        while(!Thread.currentThread().isInterrupted()) {
            notifyPassengers();
            curr.signalPassengersAtSta();
            Thread.sleep(10);
            next.claimOrWait();
            curr.notifyDeparture();
            mbta.moveTrain(t, curr, next);
            log.train_moves(t, curr, next);
            curr = next;
            next = mbta.getTNextStation(t);
        }
    */
}
