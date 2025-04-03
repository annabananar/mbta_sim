import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.List;

@FixMethodOrder( MethodSorters.NAME_ASCENDING )
public class VerifierTests {

    private static MBTA mbta;

    @BeforeClass
    public static void setUp() throws Exception {
        mbta = new MBTA();
        String line1 = "red";
        List<String> stations1 = List.of("MGH", "Park Street", "Downtown Crossing");
        mbta.addLine(line1, stations1);
        String line2 = "green";
        List<String> stations2 = List.of("Copley", "Boylston", "Park Street");
        mbta.addLine(line2, stations2);
        String passenger1 = "Anna";
        List<String> travels1 = List.of("Park Street", "Downtown Crossing");
        mbta.addJourney(passenger1, travels1);
        String passenger2 = "Brian";
        List<String> travels2 = List.of("Boylston", "Park Street", "MGH");
        mbta.addJourney(passenger2, travels2);
    }

    /*
    *   red line starts at MGH, green line starts at Copley
    *   Anna is waiting to board at Park Street
    */
    @Test
    public void test01InitConfig() {
        mbta.checkStart();
    }

    /*  red line moves from MGH to Park Street */
    @Test
    public void test02MoveRedGoodInput() {
        Train red = Train.make("red");
        Station MGH = Station.make("MGH");
        Station ParkStreet = Station.make("Park Street");
        Event moveRed = new MoveEvent(red, MGH, ParkStreet);
        moveRed.replayAndCheck(mbta);
        assert(mbta.getTCurrStation(red).equals(ParkStreet));
        assert(mbta.getTrainIndex(red) == 1);
        Station DC = Station.make("Downtown Crossing");
        assert(mbta.getTNextStation(red).equals(DC));
    }

    // Anna boards red line at Park Street
    @Test
    public void test03BoardGoodInput() {
        Passenger anna = Passenger.make("Anna");
        Train red = Train.make("red");
        Station ParkStreet = Station.make("Park Street");
        Event boardAnna = new BoardEvent(anna, red, ParkStreet);
        boardAnna.replayAndCheck(mbta);
        assert(mbta.getPassengersOnTrain(red).contains(anna));
    }

    // green line moves from Copley to Boylston
    @Test
    public void test04MoveGreenGoodInput() {
        Train green = Train.make("green");
        Station Copley = Station.make("Copley");
        Station Boylston = Station.make("Boylston");
        Event moveGreen = new MoveEvent(green, Copley, Boylston);
        moveGreen.replayAndCheck(mbta);
        assert(mbta.getTCurrStation(green).equals(Boylston));
    }

    // Brian boards green line at Boylston
    @Test
    public void test05BoardGoodInput() {
        Passenger brian = Passenger.make("Brian");
        Train green = Train.make("green");
        Station Boylston = Station.make("Boylston");
        Event boardGreen = new BoardEvent(brian, green, Boylston);
        boardGreen.replayAndCheck(mbta);
        assert(mbta.getPassengersOnTrain(green).contains(brian));
    }

    // green line FAILS to move from Boylston to Park Street
    @Test
    public void test06MoveGreenBadInput() {
        Train green = Train.make("green");
        Station Boylston = Station.make("Boylston");
        Station ParkStreet = Station.make("Park Street");
        Event moveGreen = new MoveEvent(green, Boylston, ParkStreet);
        try {
            moveGreen.replayAndCheck(mbta);
        } catch (RuntimeException e) {
            System.out.println("Exception Caught: " + e.getMessage());
        }
    }

    // red line moves from Park Street to Downtown Crossing
    @Test
    public void test07MoveRedGoodInput() {
        Train red = Train.make("red");
        Station ParkStreet = Station.make("Park Street");
        Station DC = Station.make("Downtown Crossing");
        Event moveRed = new MoveEvent(red, ParkStreet, DC);
        moveRed.replayAndCheck(mbta);
        assert(mbta.getTCurrStation(red).equals(DC));
        // red line should have its direction reversed
        assert(mbta.getTNextStation(red).equals(ParkStreet));
        Passenger anna = Passenger.make("Anna");
        // checks whether updatePassengersOnTrain() works properly
        assert(mbta.getPCurrStation(anna).equals(DC));
        assert(mbta.getPNextStation(anna) == null);
    }

    // green line moves from Boylston to Park Street
    @Test
    public void test08MoveGreenGoodInput() {
        Train green = Train.make("green");
        Station Boylston  = Station.make("Boylston");
        Station ParkStreet = Station.make("Park Street");
        Event moveGreen = new MoveEvent(green, Boylston, ParkStreet);
        moveGreen.replayAndCheck(mbta);
        assert(mbta.getTCurrStation(green).equals(ParkStreet));
        assert(mbta.getTNextStation(green).equals(Boylston));
        Passenger Brian = Passenger.make("Brian");
        Station MGH = Station.make("MGH");
        assert(mbta.getPCurrStation(Brian).equals(ParkStreet));
        assert(mbta.getPNextStation(Brian).equals(MGH));
    }

    // Anna boards off red line at Downtown Crossing
    @Test
    public void test09DeboardGoodInput() {
        Train red = Train.make("red");
        Passenger anna = Passenger.make("Anna");
        Station DC = Station.make("Downtown Crossing");
        Event deboardAnna = new DeboardEvent(anna, red, DC);
        deboardAnna.replayAndCheck(mbta);
        assert(!mbta.getPassengersOnTrain(red).contains(anna));
        assert(mbta.getPCurrStation(anna).equals(DC));
    }

    // Brian boards off green line at Park Street
    @Test
    public void test10DeboardGoodInput() {
        Train green = Train.make("green");
        Station ParkStreet = Station.make("Park Street");
        Passenger Brian = Passenger.make("Brian");
        Event deboardBrian = new DeboardEvent(Brian, green, ParkStreet);
        deboardBrian.replayAndCheck(mbta);
        assert(!mbta.getPassengersOnTrain(green).contains(Brian));
        assert(mbta.getPCurrStation(Brian).equals(ParkStreet));
    }

    // green line moves from Park Street to Boylston
    @Test
    public void test11MoveGoodInput01() {
        Train green = Train.make("green");
        Station ParkStreet = Station.make("Park Street");
        Station Boylston = Station.make("Boylston");
        Event moveGreen = new MoveEvent(green, ParkStreet, Boylston);
        moveGreen.replayAndCheck(mbta);
        assert(mbta.getTCurrStation(green).equals(Boylston));
    }

    // red line moves from Downtown Crossing to Park Street
    @Test
    public void test11MoveGoodInput02() {
        Train red = Train.make("red");
        Station DC = Station.make("Downtown Crossing");
        Station ParkStreet = Station.make("Park Street");
        Event moveRed = new MoveEvent(red, DC, ParkStreet);
        moveRed.replayAndCheck(mbta);
        assert(mbta.getTCurrStation(red).equals(ParkStreet));
        Station MGH = Station.make("MGH");
        assert(mbta.getTNextStation(red).equals(MGH));
    }

    // Brian boards red line at Park Street
    @Test
    public void test12BoardGoodInput() {
        Train red = Train.make("red");
        Station ParkStreet = Station.make("Park Street");
        Passenger Brian = Passenger.make("Brian");
        Event boardBrian = new BoardEvent(Brian, red, ParkStreet);
        boardBrian.replayAndCheck(mbta);
        assert(mbta.getPassengersOnTrain(red).contains(Brian));
    }

    // red line moves from Park Street to MGH
    @Test
    public void test13MoveGoodInput() {
        Train red = Train.make("red");
        Station ParkStreet = Station.make("Park Street");
        Station MGH = Station.make("MGH");
        Event moveRed = new MoveEvent(red, ParkStreet, MGH);
        moveRed.replayAndCheck(mbta);
        assert(mbta.getTCurrStation(red).equals(MGH));
        assert(mbta.getTNextStation(red).equals(ParkStreet));
    }

    // Brian boards off red line at MGH
    @Test
    public void test14DeboardGoodInput() {
        Train red = Train.make("red");
        Station MGH = Station.make("MGH");
        Passenger Brian = Passenger.make("Brian");
        Event deboardBrian = new DeboardEvent(Brian, red, MGH);
        deboardBrian.replayAndCheck(mbta);
        assert(!mbta.getPassengersOnTrain(red).contains(Brian));
        assert(mbta.getPCurrStation(Brian).equals(MGH));
        assert(mbta.getPNextStation(Brian) == null);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        mbta.checkEnd();
        mbta.reset();
    }
}
