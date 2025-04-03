import org.junit.*;

import java.util.ArrayList;
import java.util.List;

public class MBTATests {

    /* checks if loadConfig correctly converts JSON file into java objects */
    @Test
    public void testMBTAConfig() {
        MBTA mbta = new MBTA();
        mbta.loadConfig("sample.json");
    }

    @Test
    public void testAddLine() {
        MBTA mbta = new MBTA();
        String name = "red";
        List<String> stations = new ArrayList<>(List.of("Davis", "Porter", "Harvard", "Central"));
        mbta.addLine(name, stations);
        Train train = Train.make(name);
        /* check if the current station has been loaded correctly */
        Station start = mbta.getTCurrStation(train);
        assert(start.toString().equals(stations.getFirst()));
    }

    @Test
    public void testAddJourney() {
        MBTA mbta = new MBTA();
        String lineName = "red";
        List<String> tStations = new ArrayList<>(List.of("Davis", "Porter", "Harvard", "Central"));
        mbta.addLine(lineName, tStations);
        String name = "Anna";
        List<String> pStations = new ArrayList<>(List.of( "Porter", "Harvard"));
        mbta.addJourney(name, pStations);
        Passenger anna = Passenger.make("Anna");
        /* check if passenger is placed at the correct starting station */
        List<Passenger> startStationPassenger = mbta.getStartingStation(anna);
        assert(startStationPassenger.contains(anna));
    }
}
