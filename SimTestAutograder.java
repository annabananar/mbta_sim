import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.List;


public class SimTestAutograder {

    private MBTA mbta;
    private Log log;

    @Before
    public void setUp() {
        mbta = new MBTA();
        log = new Log();
    }

    @Before
    public void tearDown() {
        Sim.clearThreads();
        mbta.reset();
    }

    @Test(timeout = 7000)
    public void test01LargeInput() {
        String red = "red";
        List<String> redSta = List.of("A", "B", "D", "G", "F");
        mbta.addLine(red, redSta);
        String green = "green";
        List<String> greenSta = List.of("H", "G", "E", "B", "C");
        mbta.addLine(green, greenSta);
        String orange = "orange";
        List<String> orangeSta = List.of("L", "K", "J", "I", "H");
        mbta.addLine(orange, orangeSta);
        String blue = "blue";
        List<String> blueSta = List.of("R", "S", "P", "N", "M", "F");
        mbta.addLine(blue, blueSta);
        String purple = "purple";
        List<String> purpleSta = List.of("O", "N", "Q", "S", "T", "L");
        mbta.addLine(purple, purpleSta);

        String a = "Aardvark";
        List<String> aSta = List.of("R", "S", "T");
        mbta.addJourney(a, aSta);
        String b = "Bear";
        List<String> bSta = List.of("R", "F", "G", "H");
        mbta.addJourney(b, bSta);
        String c = "Cow";
        List<String> cSta = List.of("R", "S", "L", "H");
        mbta.addJourney(c, cSta);
        String d = "Dog";
        List<String> dSta = List.of("A", "B", "G");
        mbta.addJourney(d, dSta);
        String e = "Elephant";
        List<String> eSta = List.of("D", "F", "N", "T");
        mbta.addJourney(e, eSta);
        String f = "Frog";
        List<String> fSta = List.of("O", "N", "F", "G", "H");
        mbta.addJourney(f, fSta);
        String g = "Giraffe";
        List<String> gSta = List.of("O", "L", "H");
        mbta.addJourney(g, gSta);
        String h = "Horse";
        List<String> hSta = List.of("M", "N");
        mbta.addJourney(h, hSta);
        String i = "Iguana";
        List<String> iSta = List.of("P", "F", "B", "C");
        mbta.addJourney(i, iSta);
        String j = "Jaguar";
        List<String> jSta = List.of("H", "L");
        mbta.addJourney(j, jSta);
        String k = "Koala";
        List<String> kSta = List.of("L", "T");
        mbta.addJourney(k, kSta);
        String l = "Lamprey";
        List<String> lSta = List.of("L", "H", "G", "F", "S", "T");
        mbta.addJourney(l, lSta);

        Sim.run_sim(mbta, log);
    }

    @Test(timeout = 5000)
    public void test02SmallInput() {
        String red = "red";
        List<String> redStations = List.of("D", "C", "A");
        String blue = "blue";
        List<String> blueStations = List.of("A", "B", "D");
        mbta.addLine(red, redStations);
        mbta.addLine(blue, blueStations);
        String alice = "Alice";
        List<String> aliceStations = List.of("A", "C");
        String bob = "Bob";
        List<String> bobStations = List.of("A", "B");
        String carol = "Carol";
        List<String> carolStations = List.of("B", "A");
        String david = "David";
        List<String> davidStations = List.of("C", "A");
        mbta.addJourney(alice, aliceStations);
        mbta.addJourney(bob, bobStations);
        mbta.addJourney(carol, carolStations);
        mbta.addJourney(david, davidStations);

        Sim.run_sim(mbta, log);
    }

    @Test(timeout = 6000)
    public void test03MediumInput() {
        String red = "red";
        List<String> redStations = List.of("Davis", "Harvard", "Kendall", "Park", "GREEN_PASS",
                "Downtown Crossing", "South Station", "Broadway", "Andrew", "JFK");
        String blue = "blue";
        List<String> blueStations = List.of("Bowdoin", "Government Center", "ORANGE_PASS", "State",
                "Aquarium", "Maverick", "Airport");
        String orange = "orange";
        List<String> orangeStations = List.of("Ruggles", "Back Bay", "Tufts Medical Center", "Chinatown",
                "Downtown Crossing", "RED_PASS", "State", "North Station", "Sullivan");
        String green = "green";
        List<String> greenStations = List.of("Tufts", "East Somerville", "Lechmere", "North Station",
                "Government Center", "BLUE_PASS", "Park", "Boylston", "Arlington", "Copley");
        String alice = "Alice";
        List<String> aliceStations = List.of("Davis", "Kendall");
        String carol = "Carol";
        List<String> carolStations = List.of("Maverick", "Government Center", "Tufts");
        String bob = "Bob";
        List<String> bobStations = List.of("Park", "Tufts");

        mbta.addLine(red, redStations);
        mbta.addLine(blue, blueStations);
        mbta.addLine(orange, orangeStations);
        mbta.addLine(green, greenStations);
        mbta.addJourney(alice, aliceStations);
        mbta.addJourney(bob, bobStations);
        mbta.addJourney(carol, carolStations);

        Sim.run_sim(mbta, log);
    }

    @Test(timeout = 4000)
    public void test04ClusteredInput() {
        String purple = "purple";
        List<String> purpleSta = List.of("North Station", "Ipswich", "Rockport");
        String green = "green";
        List<String> greenSta = List.of("Haymarket", "North Station", "Lechmere");
        String orange = "orange";
        List<String> orangeSta = List.of("Tufts Medical Center", "Chinatown", "North Station");
        mbta.addLine(purple, purpleSta);
        mbta.addLine(green, greenSta);
        mbta.addLine(orange, orangeSta);
        String alice = "Alice";
        List<String> aliceSta = List.of("Haymarket", "North Station", "Chinatown");
        mbta.addJourney(alice, aliceSta);
        String bob = "Bob";
        List<String> bobSta = List.of("Ipswich", "North Station", "Haymarket");
        mbta.addJourney(bob, bobSta);
        Sim.run_sim(mbta, log);
    }
}
