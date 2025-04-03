import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.List;

@FixMethodOrder( MethodSorters.NAME_ASCENDING )
public class SimTests {
    private MBTA mbta;
    private Log log;

    @Before
    public void setUp() {
        mbta = new MBTA();
        log = new Log();
        String line1 = "red";
        List<String> stations = List.of("Harvard", "Central", "MIT");
        mbta.addLine(line1, stations);
    }

    @Test
    public void test01SimpleInput() {
        String passenger1 = "Abby";
        List<String> journeys = List.of("Central", "MIT");
        mbta.addJourney(passenger1, journeys);
        Sim.run_sim(mbta, log);
    }

    /*
     *   pink line's next station will be red line's starting station
     *   hence pink line may need to wait at Porter before red line leaves
    */
    @Test
    public void test02LittleComplexInput() {
        String passenger1 = "Abby";
        List<String> journey1 = List.of("Central", "MIT");
        String pink = "pink";
        List<String> stations = List.of("Porter", "Harvard", "Davis");
        mbta.addLine(pink, stations);
        mbta.addJourney(passenger1, journey1);
        Sim.run_sim(mbta, log);
    }

    /*
    *   Abby now starts at Harvard, does a line transfer at Harvard, and goes to Central
    *
    */
    @Test
    public void test03EvenMoreComplexInput() {
        String pink = "pink";
        List<String> stations = List.of("Porter", "Harvard", "Davis");
        mbta.addLine(pink, stations);
        String passenger1 = "Abby";
        List<String> journey1 = List.of("Porter", "Harvard", "Central");
        mbta.addJourney(passenger1, journey1);
        Sim.run_sim(mbta, log);
    }
}
