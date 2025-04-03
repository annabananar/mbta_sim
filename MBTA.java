import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MBTA {

    // lines and journeys loaded from config file
    public final Map<Train, List<Station>> lines;
    public final Map<Passenger, List<Station>> journeys;

    // each train and their current station
    private final Map<Train, Station> tCurrStation;
    // each passenger and their current station
    private final Map<Passenger, Station> pCurrStation;

    // each train and their current station index
    private final Map<Train, Integer> trainIndex;
    // each train and their current direction (1 = in reverse)
    private final Map<Train, Integer> trainDirection;

    // passengers on each train
    private final Map<Train, List<Passenger>> trainPassengers;
    // passengers at each station
    private final Map<Station, List<Passenger>> staPassengers;

    // each passenger and their remaining stations
    private final Map<Passenger, Integer> pIndex;

    // 1-1 mapping between entities and threads
    private final Map<Train, TransitTrain> trainMapping;
    private final Map<Passenger, TransitPassenger> passengerMapping;

    // each station and their associated locks/conditions
    private final Map<Station, Map<Lock, List<Condition>>> stationLocks;

    // Creates an initially empty simulation
    public MBTA() {
        lines = new HashMap<>();
        journeys = new HashMap<>();
        tCurrStation = new ConcurrentHashMap<>();
        pCurrStation = new ConcurrentHashMap<>();
        trainIndex = new ConcurrentHashMap<>();
        trainDirection = new ConcurrentHashMap<>();
        trainPassengers = new ConcurrentHashMap<>();
        staPassengers = new ConcurrentHashMap<>();
        pIndex = new ConcurrentHashMap<>();
        trainMapping = new HashMap<>();
        passengerMapping = new HashMap<>();
        stationLocks = new ConcurrentHashMap<>();
    }

    // Adds a new transit line with given name and stations
    public void addLine(String name, List<String> stations) {
        Train t = Train.make(name);
        /* Initialize states associated with trains */
        lines.putIfAbsent(t, new ArrayList<>());
        trainPassengers.putIfAbsent(t, new ArrayList<>());
        /* Initialize the stations */
        for(String station : stations) {
            Station currSta = Station.make(station);
            lines.get(t).add(currSta);
            staPassengers.put(currSta, new ArrayList<>());
            setUpLocks(currSta);
        }
        /* Set up starting station & curr station index */
        tCurrStation.put(t, lines.get(t).getFirst());
        trainIndex.put(t, 0);
        trainDirection.put(t, 0);
    }

    /*
     *   configures locks & conditions for each station
     *   tCondition - train threads are added to this when they need to wait for the station
     *   to become available;
     *   pCondition - passenger threads are added to this when they need to wait for trains
     *   to arrive at the station (then check whether they should board)
     */
    public void setUpLocks(Station currSta) {
        if(!stationLocks.containsKey(currSta)) {
            Map<Lock, List<Condition>> lockMap = new HashMap<>();
            Lock stationLock = new ReentrantLock();
            Condition trains = stationLock.newCondition();
            Condition passengers = stationLock.newCondition();
            List<Condition> condList = new ArrayList<>(List.of(trains, passengers));
            lockMap.put(stationLock, condList);
            stationLocks.put(currSta, lockMap);
        }
    }

    // Adds a new planned journey to the simulation
    public void addJourney(String name, List<String> stations) {
        Passenger p = Passenger.make(name);
        journeys.putIfAbsent(p, new ArrayList<>());
        for(String station : stations) {
            Station currSta = Station.make(station);
            journeys.get(p).add(currSta);
        }
        /* Set up starting station */
        staPassengers.get(journeys.get(p).getFirst()).add(p);
        pIndex.put(p, 0);
        pCurrStation.put(p, journeys.get(p).getFirst());
    }

    // Return normally if initial simulation conditions are satisfied, otherwise
    // raises an exception
    public void checkStart() {
        /* if each train is at its starting station */
        for(Train t : lines.keySet()) {
            if(!tCurrStation.get(t).equals(lines.get(t).getFirst())) {
                throw new RuntimeException("Line " + t + " is not at its starting station");
            }
        }
        /* if each passenger is at their starting station */
        for(Passenger p : journeys.keySet()) {
            Station start = journeys.get(p).getFirst();
            if(!staPassengers.get(start).contains(p)) {
                throw new RuntimeException("Passenger " + p + " is not at their starting station");
            }
        }
    }

    // Return normally if final simulation conditions are satisfied, otherwise
    // raises an exception
    public void checkEnd() {
        /* if any passengers have any stations remaining */
        for(Passenger p : journeys.keySet()) {
            Station end = journeys.get(p).getLast();
            int pCurrIndex = pIndex.get(p);
            if(!staPassengers.get(end).contains(p)) {
                throw new RuntimeException("Passenger " + p + " is not at their final destination");
            }
        }
    }

    // reset to an empty simulation
    public void reset() {
        lines.clear();
        journeys.clear();
        tCurrStation.clear();
        pCurrStation.clear();
        trainIndex.clear();
        pIndex.clear();
        trainPassengers.clear();
        staPassengers.clear();
        trainMapping.clear();
        passengerMapping.clear();
        stationLocks.clear();
        Train.clearCache();
        Station.clearCache();
        Passenger.clearCache();
    }

    // adds simulation configuration from a file
    public void loadConfig(String filename) {
        try {
            String fileInJson = new String(Files.readAllBytes(Paths.get(filename)));
            Gson gson = new Gson();
            MBTAJson mbtaJson = gson.fromJson(fileInJson, MBTAJson.class);

            for(Map.Entry<String, List<String>> line : mbtaJson.lines.entrySet()) {
                String lineName = line.getKey();
                List<String> stations = line.getValue();
                addLine(lineName, stations);
            }
            for(Map.Entry<String, List<String>> journey : mbtaJson.trips.entrySet()) {
                String passengerName = journey.getKey();
                List<String> stations = journey.getValue();
                addJourney(passengerName, stations);
            }
        } catch (IOException e) {
          System.out.println("Error reading config file: " + e.getMessage());
        }
    }

    /* helper functions */

    public Station getTCurrStation(Train t) {
        return tCurrStation.get(t);
    }

    public Station getPCurrStation(Passenger p) {
        return pCurrStation.get(p);
    }

    public Train getTrainAtSta(Station s) {
        for(Train t : tCurrStation.keySet()) {
            if(tCurrStation.get(t).equals(s)) return t;
        }
        return null;
    }

    public Station getTNextStation(Train t) {
        int direction = trainDirection.get(t);
        int currIndex = trainIndex.get(t);
        /* treat start and end stations distinctly */
        if(currIndex == 0) return lines.get(t).get(currIndex + 1);      // start
        if(currIndex == lines.get(t).size() - 1) {
            return lines.get(t).get(currIndex - 1);       // end
        }
        if(direction == 0) {
            return lines.get(t).get(currIndex + 1);
        } else {
            return lines.get(t).get(currIndex - 1);
        }
    }

    public Station getPNextStation(Passenger p) {
        int currIndex = pIndex.get(p);
        List<Station> stations = journeys.get(p);
        if(currIndex >= stations.size() - 1) return null;
        return stations.get(currIndex + 1);
    }

    public boolean getStaAvailability(Station dst) {
        for(Station curr : tCurrStation.values()) {
            if(curr.equals(dst)) return false;
        }
        return true;
    }

    public void moveTrain(Train t, Station prev, Station next) {
        tCurrStation.put(t, next);
        int currIndex = trainIndex.get(t);
        int currDir = trainDirection.get(t);
        if (currDir == 0) {
            if (currIndex == lines.get(t).size() - 1) {
                /* reverses direction from front to back */
                trainIndex.put(t, currIndex - 1);
                trainDirection.put(t, 1);
            } else {
                trainIndex.put(t, currIndex + 1);
            }
        } else if (currDir == 1) {
            if (currIndex == 0) {
                /* reverses direction from back to front */
                trainIndex.put(t, currIndex + 1);
                trainDirection.put(t, 0);
            } else {
                trainIndex.put(t, currIndex - 1);
            }
        }
    }

    public void boardPassenger(Train t, Passenger p, Station s) {
        staPassengers.get(s).remove(p);
        trainPassengers.get(t).add(p);
    }

    public void deboardPassenger(Train t, Passenger p, Station s) {
        trainPassengers.get(t).remove(p);
        staPassengers.get(s).add(p);
        pCurrStation.put(p, s);
        int currIndex = pIndex.get(p);
        pIndex.put(p, ++currIndex);
    }

    public int getTrainIndex(Train t) {
        return trainIndex.get(t);
    }

    public List<Passenger> getPassengersOnTrain(Train t) {
        return trainPassengers.get(t);
    }

    public List<Passenger> getStartingStation(Passenger p) {
        return staPassengers.get(journeys.get(p).getFirst());
    }

    public Train isPassengerOnTrain(Passenger p) {
        for(Train t : lines.keySet()) {
            if(getPassengersOnTrain(t).contains(p)) return t;
        }
        return null;
    }

    public TransitTrain getTrainThread(Train t) {
        return trainMapping.get(t);
    }

    public void addToTrainMapping(Train t, TransitTrain tThread) {
        trainMapping.putIfAbsent(t, tThread);
    }

    public void addToPassengerMapping(Passenger p, TransitPassenger pThread) {
        passengerMapping.putIfAbsent(p, pThread);
    }

    /* checks whether t's future stations contain p's next station */
    public boolean isRightTrain(Train t, Passenger p) {
        Station pNext = getPNextStation(p);
        if(pNext == null) return true;
        int dir = trainDirection.get(t);
        int currIndex = trainIndex.get(t);
        List<Station> tStations = lines.get(t);
        if((dir == 0 && currIndex != tStations.size() - 1) || (dir == 1 && currIndex == 0)) {
            for(int i = currIndex; i < tStations.size(); i++) {
                Station curr = tStations.get(i);
                if(curr.equals(pNext)) return true;
            }
        } else {
            for(int i = currIndex; i >= 0; i--) {
                Station curr = tStations.get(i);
                if(curr.equals(pNext)) return true;
            }
        }
        return false;
    }

    public Train NextTrainToBoard(Passenger p, Station curr) {
        Station next = getPNextStation(p);
        if(next == null) return null;
        for(Train t : lines.keySet()) {
            List<Station> tStations = lines.get(t);
            if(tStations.contains(next) && tStations.contains(curr)) return t;
        }
        return null;
    }

    public Lock getStationLock(Station s) {
        return (Lock) stationLocks.get(s).keySet().toArray()[0];
    }

    public Condition getTConditionAtS(Station s) {
        Map<Lock, List<Condition>> sta = stationLocks.get(s);
        Lock trainLock = sta.keySet().iterator().next();
        return sta.get(trainLock).getFirst();
    }

    public Condition getPConditionAtS(Station s) {
        Map<Lock, List<Condition>> sta = stationLocks.get(s);
        Lock trainLock = sta.keySet().iterator().next();
        return sta.get(trainLock).getLast();
    }
}
