import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class TransitPassenger implements TransitThread {

    private Passenger p;
    private MBTA mbta;
    private Log log;
    private Station curr;

    public TransitPassenger(Passenger p, MBTA mbta, Log log, Station curr) {
        this.p = p;
        this.mbta = mbta;
        this.log = log;
        this.curr = curr;
    }

    @Override
    public void run() {
        try {
            while(curr != null) {
                // System.out.println(p + " is currently at: " + curr);
                Train t = mbta.isPassengerOnTrain(p);
                Station next = mbta.getPNextStation(p);
                // System.out.println(p + "'s next station: " + next);
                if(t != null) {
                    waitToDeboard(t, next);
                } else {
                    Train nextT = mbta.NextTrainToBoard(p, curr);
                    // System.out.println(p + "'s next train to board: " + nextT);
                    if(nextT == null) {
                        throw new RuntimeException("Something went wrong with running passenger thread");
                    }
                    waitForEnboard(next, nextT);
                }
                curr = next;
            }
        } catch (InterruptedException | RuntimeException e) {
            System.out.println(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void waitForEnboard(Station next, Train t) throws InterruptedException {
        Lock currLock = mbta.getStationLock(curr);
        Condition pCondition = mbta.getPConditionAtS(curr);
        currLock.lock();
        try {
            // System.out.println(p + " locks " + curr + " in waitForEnboard");
            while(mbta.getTrainAtSta(curr) == null || !mbta.getTrainAtSta(curr).equals(t)) {
                // System.out.println(p + " unlocks " + curr + " going into await");
                pCondition.await();
            }
            log.passenger_boards(p, t, curr);
            mbta.boardPassenger(t, p, curr);
        } catch (InterruptedException e) {
            System.err.println("TransitPassenger: Something went wrong when boarding");
        } finally {
            currLock.unlock();
            // System.out.println(p + " unlocks " + curr + " after boarding");
        }
    }

    private void waitToDeboard(Train t, Station next) throws InterruptedException {
        Lock nextLock = mbta.getStationLock(next);
        Condition pCondition = mbta.getPConditionAtS(next);
        nextLock.lock();
        try {
            // System.out.println(p + " locks " + curr);
            while(!mbta.getTCurrStation(t).equals(next)) {
                // System.out.println(p + " unlocks " + curr + " going into await");
                pCondition.await();
            }
            log.passenger_deboards(p, t, next);
            mbta.deboardPassenger(t, p, next);
        } catch (InterruptedException e) {
            System.err.println("TransitPassenger: Something went wrong when boarding off");
        } finally {
            nextLock.unlock();
            // System.out.println(p + " unlocks " + curr + " after deboarding");
        }
    }

    /* while(!mbta.getTrainAtSta(curr).equals(nextT)) {
            curr.awaitTrains();
            Train t = mbta.getTrainAtSta(curr);
            if(t == null) return;
            try {
                BoardEvent boardP = new BoardEvent(p, t, curr);
                boardP.replayAndCheck(mbta);
                log.passenger_boards(p, t, curr);
            } catch (RuntimeException e) {
                // passenger shouldn't board the current train, continue waiting
            }
    } */

    /* while(!mbta.getTCurrStation(t).equals(next)) {
            // tThread.waitForNext();
            curr = mbta.getTCurrStation(t);
            try {
                DeboardEvent deboardP = new DeboardEvent(p, t, curr);
                deboardP.replayAndCheck(mbta);
                log.passenger_deboards(p, t, curr);
            } catch (RuntimeException e) {
                // passenger shouldn't get off the train at this station, continue waiting
            }
    } */
}
