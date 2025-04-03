import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Sim {

    private final static List<Thread> tThreads = new ArrayList<>();
    private final static List<Thread> pThreads = new ArrayList<>();

    public static void run_sim(MBTA mbta, Log log) {
        for(Train t : mbta.lines.keySet()) {
            TransitTrain newTrain = new TransitTrain(t, mbta, log, mbta.getTCurrStation(t));
            tThreads.add(new Thread(newTrain));
            mbta.addToTrainMapping(t, newTrain);
        }
        for(Passenger p : mbta.journeys.keySet()) {
            TransitPassenger newPassenger = new TransitPassenger(p, mbta, log, mbta.getPCurrStation(p));
            pThreads.add(new Thread(newPassenger));
            mbta.addToPassengerMapping(p, newPassenger);
        }
        tThreads.forEach(Thread::start);
        pThreads.forEach(Thread::start);
        try {
            for(Thread p : pThreads) {
                p.join();
            }
        } catch (InterruptedException e) {
            System.err.println("Sim interrupted: " + e.getMessage());
        }
        // System.out.println("Sim is done, interrupting train threads");
        tThreads.forEach(Thread::interrupt);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("usage: ./sim <config file>");
            System.exit(1);
        }

        MBTA mbta = new MBTA();
        mbta.loadConfig(args[0]);

        Log log = new Log();

        run_sim(mbta, log);

        String s = new LogJson(log).toJson();
        PrintWriter out = new PrintWriter("log.json");
        out.print(s);
        out.close();

        mbta.reset();
        mbta.loadConfig(args[0]);
        Verify.verify(mbta, log);
    }

    public static void clearThreads() {
        tThreads.clear();
        pThreads.clear();
    }
}
