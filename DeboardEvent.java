import java.util.*;

public class DeboardEvent implements Event {
    public final Passenger p;
    public final Train t;
    public final Station s;

    public DeboardEvent(Passenger p, Train t, Station s) {
        this.p = p; this.t = t; this.s = s;
    }

    public boolean equals(Object o) {
        if (o instanceof DeboardEvent e) {
          return p.equals(e.p) && t.equals(e.t) && s.equals(e.s);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(p, t, s);
    }

    public String toString() {
        return "Passenger " + p + " deboards " + t + " at " + s;
    }

    public List<String> toStringList() {
        return List.of(p.toString(), t.toString(), s.toString());
    }

    public void replayAndCheck(MBTA mbta) {
        Station correctNext = mbta.getPNextStation(p);
        if(!mbta.getPassengersOnTrain(t).contains(p)) {
            throw new RuntimeException("Passenger " + p + " not on the train");
        }
        if(!mbta.getTCurrStation(t).equals(s)) {
            throw new RuntimeException("Train must be at current station to deboard passengers");
        }
        if(!correctNext.equals(s)) {
            throw new RuntimeException("Passenger shouldn't deboard at current station");
        }
        mbta.deboardPassenger(t, p, s);
    }
}
